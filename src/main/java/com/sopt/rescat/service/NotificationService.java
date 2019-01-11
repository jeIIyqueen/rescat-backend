package com.sopt.rescat.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.sopt.rescat.domain.*;
import com.sopt.rescat.domain.enums.RequestStatus;
import com.sopt.rescat.domain.enums.RequestType;
import com.sopt.rescat.domain.log.UserNotificationLog;
import com.sopt.rescat.domain.request.CareTakerRequest;
import com.sopt.rescat.domain.request.MapRequest;
import com.sopt.rescat.exception.InvalidValueException;
import com.sopt.rescat.exception.NotMatchException;
import com.sopt.rescat.repository.NotificationRepository;
import com.sopt.rescat.repository.UserNotificationLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

@Slf4j
@Service
public class NotificationService {
    private static final String PROJECT_ID = "rescat";
    private static final String BASE_URL = "https://fcm.googleapis.com";
    private static final String FCM_SEND_ENDPOINT = "/v1/projects/" + PROJECT_ID + "/messages:send";
    private static final String FCM_GROUP_ENDPOINT = "/fcm/notification";

    private static final String MESSAGING_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
    private static final String[] SCOPES = {MESSAGING_SCOPE};

    private static final String MESSAGE_KEY = "message";

    private UserNotificationLogRepository userNotificationLogRepository;
    private NotificationRepository notificationRepository;

    public NotificationService(final UserNotificationLogRepository userNotificationLogRepository,
                               final NotificationRepository notificationRepository) {
        this.userNotificationLogRepository = userNotificationLogRepository;
        this.notificationRepository = notificationRepository;
    }

    // [START retrieve_access_token]
    private static String getAccessToken() throws IOException {
        GoogleCredential googleCredential = GoogleCredential
                .fromStream(new FileInputStream("src/main/resources/rescat-firebase-adminsdk-v7wju-3d87a2b9d4.json"))
                .createScoped(Arrays.asList(SCOPES));
        googleCredential.refreshToken();
        return googleCredential.getAccessToken();
    }
    // [END retrieve_access_token]

    private static HttpURLConnection getConnection(Boolean sendType) throws IOException {
        // [START use_access_token]
        URL url;
        if (!sendType)
            url = new URL(BASE_URL + FCM_GROUP_ENDPOINT);
        else
            url = new URL(BASE_URL + FCM_SEND_ENDPOINT);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestProperty("Authorization", "Bearer " + getAccessToken());
        httpURLConnection.setRequestProperty("Content-Type", "application/json");
        return httpURLConnection;
    }

    private static void sendPush(JsonObject fcmMessage, Boolean sendType) throws IOException {
        HttpURLConnection connection;
        if (!sendType)
            connection = getConnection(sendType);

        else
            connection = getConnection(sendType);
        connection.setDoOutput(true);

        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(fcmMessage.toString().getBytes("UTF-8"));
        outputStream.flush();
        outputStream.close();

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpStatus.OK.value()) {
            String response = inputstreamToString(connection.getInputStream());
            System.out.println("Message sent to Firebase for delivery, response:");
            System.out.println(response);

        } else {
            System.out.println("Unable to send message to Firebase:");
            String response = inputstreamToString(connection.getErrorStream());
            System.out.println(response);
        }
    }

    public static void writePush(String instanceToken, String body) throws IOException {
        JsonObject Message = buildOverrideMessage(instanceToken, body);
        System.out.println("FCM request body for override message:");
        prettyPrint(Message);
        sendPush(Message, true);
    }

    private static JsonObject buildOverrideMessage(String instanceToken, String body) {
        JsonObject jNotificationMessage = buildNotificationMessage(instanceToken, body);

        JsonObject messagePayload = jNotificationMessage.get(MESSAGE_KEY).getAsJsonObject();
        messagePayload.add("android", buildAndroidOverridePayload());

        JsonObject apnsPayload = new JsonObject();
        apnsPayload.add("headers", buildApnsHeadersOverridePayload());
        apnsPayload.add("payload", buildApsOverridePayload());

        messagePayload.add("apns", apnsPayload);

        jNotificationMessage.add(MESSAGE_KEY, messagePayload);

        return jNotificationMessage;
    }

    private static JsonObject buildAndroidOverridePayload() {
        JsonObject androidNotification = new JsonObject();
        androidNotification.addProperty("click_action", "android.intent.action.MAIN");

        JsonObject androidNotificationPayload = new JsonObject();
        androidNotificationPayload.add("notification", androidNotification);
        androidNotificationPayload.addProperty("priority", "normal");

        return androidNotificationPayload;
    }

    private static JsonObject buildApnsHeadersOverridePayload() {
        JsonObject apnsHeaders = new JsonObject();
        apnsHeaders.addProperty("apns-priority", "5");

        return apnsHeaders;
    }

    private static JsonObject buildApsOverridePayload() {
        JsonObject badgePayload = new JsonObject();
        badgePayload.addProperty("badge", 1);

        JsonObject apsPayload = new JsonObject();
        apsPayload.add("aps", badgePayload);

        return apsPayload;
    }


    private static JsonObject buildNotificationMessage(String instanceToken, String body) {
        JsonObject jNotification = new JsonObject();

        jNotification.addProperty("body", body);

        JsonObject jMessage = new JsonObject();
        jMessage.addProperty("token", instanceToken);
        jMessage.add("notification", jNotification);

        JsonObject jFcm = new JsonObject();
        jFcm.add(MESSAGE_KEY, jMessage);

        return jFcm;
    }

    private static String inputstreamToString(InputStream inputStream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        Scanner scanner = new Scanner(inputStream);
        while (scanner.hasNext()) {
            stringBuilder.append(scanner.nextLine());
        }
        return stringBuilder.toString();
    }


    private static void prettyPrint(JsonObject jsonObject) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println(gson.toJson(jsonObject) + "\n");
    }

    @Transactional
    public void pushNotification(User receivingUser, Notification notification) {
        if (receivingUser.getInstanceToken() == null)
            return;
        userNotificationLogRepository.save(
                UserNotificationLog.builder()
                        .receivingUser(receivingUser)
                        .notification(notification)
                        .isChecked(RequestStatus.DEFER.getValue())
                        .build());
        try {
            writePush(receivingUser.getInstanceToken(), notification.getContents());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Transactional
    public List<UserNotificationLog> getNotification(User user) {

        List<UserNotificationLog> userNotificationLogs = userNotificationLogRepository.findByReceivingUserOrderByCreatedAtDesc(user);
        userNotificationLogs.stream()
                .filter(userNotificationLog -> userNotificationLog.getNotification().isTargetIdxNull())
                .forEach(UserNotificationLog::updateIsChecked);

        return userNotificationLogs;
    }

    @Transactional
    public Notification updateIsChecked(Long idx, User user) {

        Notification notification = notificationRepository.findById(idx)
                .orElseThrow(() -> new NotMatchException("idx", "idx에 해당하는 알림이 존재하지 않습니다."));

        UserNotificationLog notificationLog = userNotificationLogRepository.findByNotificationAndReceivingUser(notification, user);

        if (notificationLog == null)
            throw new NotMatchException("idx", "해당 idx 알림은 사용자가 받은 알림이 아닙니다.");

        notificationLog.updateIsChecked();

        return notification;
    }

    public <T> void send(T object, User receivingUser) {
        Notification notification;

        if (object instanceof Funding)
            notification = createNotification((Funding) object, receivingUser);
        else if (object instanceof CarePost)
            notification = createNotification((CarePost) object, receivingUser);
        else if (object instanceof CareTakerRequest)
            notification = createNotification((CareTakerRequest) object, receivingUser);
        else if (object instanceof MapRequest)
            notification = createNotification((MapRequest) object, receivingUser);
        else if (object instanceof CareApplication)
            notification = createNotification((CareApplication) object, receivingUser);
        else if (object instanceof CarePostComment)
            notification = createNotification((CarePostComment) object);
        else if (object instanceof FundingComment)
            notification = createNotification((FundingComment) object);
        else
            throw new InvalidValueException("notification", "알림값이 잘못 되었습니다.");

        notificationRepository.save(notification);
        pushNotification(receivingUser, notification);
    }

    private Notification createNotification(Funding funding, User receivingUser) {
        if (funding.getIsConfirmed().equals(RequestStatus.CONFIRM.getValue())) {
            return Notification.builder()
                    .targetType(RequestType.FUNDING)
                    .targetIdx(funding.getIdx())
                    .contents(receivingUser.getNickname() + "님의 후원글 신청이 승인되었습니다.\n회원님의 목표금액 달성을 응원합니다.")
                    .build();
        }
        return Notification.builder()
                .contents(receivingUser.getNickname() + "님의 후원글 신청이 거절되었습니다.\n별도의 문의사항은 마이페이지 > 문의하기 탭을 이용해주시기 바랍니다.")
                .build();
    }

    public Notification createNotification(CarePost carePost, User receivingUser) {
        String requestType;
        RequestType requestTypeValue;
        if (carePost.getType() == 0) {
            requestType = "입양";
            requestTypeValue = RequestType.CAREPOST;
        } else {
            requestType = "임시보호";
            requestTypeValue = RequestType.TEMPORALCAREPOST;
        }

        if (carePost.getIsConfirmed().equals(RequestStatus.CONFIRM.getValue())) {
            return Notification.builder()
                    .targetType(requestTypeValue)
                    .targetIdx(carePost.getIdx())
                    .contents(receivingUser.getNickname() + "님의 " + requestType + " 등록 신청이 승인되었습니다.\n좋은 " + requestType + "자를 만날 수 있기를 응원합니다.")
                    .build();
        }
        return Notification.builder()
                .contents(receivingUser.getNickname() + "님의 " + requestType + " 등록 신청이 거절되었습니다.\n별도의 문의사항은 마이페이지 > 문의하기 탭을 이용해주시기 바랍니다.")
                .build();
    }

    private Notification createNotification(CareTakerRequest careTakerRequest, User receivingUser) {
        //지역추가 or 케테 신청
        String requestType = (careTakerRequest.getType() == 0) ? "케어테이커" : "활동지역 추가";

        if (careTakerRequest.getIsConfirmed().equals(RequestStatus.CONFIRM.getValue()))
            return Notification.builder()
                    .contents(receivingUser.getNickname() + "님의 " + requestType + " 신청이 승인되었습니다.\n앞으로 활발한 활동 부탁드립니다.")
                    .build();
        return Notification.builder()
                .contents(receivingUser.getNickname() + "님의 " + requestType + " 신청이 거절되었습니다.\n별도의 문의사항은 마이페이지 > 문의하기 탭을 이용해주시기 바랍니다.")
                .build();
    }

    private Notification createNotification(MapRequest mapRequest, User receivingUser) {
        String requestType = (mapRequest.getRequestType() == 0) ? "등록" : "수정";
        String registerType;
        if (mapRequest.getRegisterType() == 0)
            registerType = "배식소";
        else if (mapRequest.getRegisterType() == 1)
            registerType = "병원";
        else
            registerType = "고양이";

        if (mapRequest.getIsConfirmed().equals(RequestStatus.CONFIRM.getValue()))
            return Notification.builder()
                    .contents(receivingUser.getNickname() + "님의 " + registerType + requestType + " 요청이 거절되었습니다.\n별도의 문의사항은 마이페이지 > 문의하기 탭을 이용해주시기 바랍니다.")
                    .build();
        return Notification.builder()
                .contents(receivingUser.getNickname() + "님의 " + registerType + requestType + " 요청이 승인되었습니다.")
                .build();
    }

    private Notification createNotification(CareApplication careApplication, User receivingUser) {
        String requestType;

        if (careApplication.getIsAccepted()) {
            requestType = (careApplication.getCarePost().getType() == 0) ? "입양" : "임시보호";
            return Notification.builder()
                    .contents(receivingUser.getNickname() + "님의 " + requestType + " 신청이 승인되었습니다.\n당신의 아름다운 결정을 지지합니다.")
                    .build();
        } else {
            requestType = (careApplication.getCarePost().getType() == 0) ? "입양을" : "임시보호를";
            return Notification.builder()
                    .targetIdx(careApplication.getCarePost().getIdx())
                    .targetType(RequestType.CAREAPPLICATION)
                    .contents(careApplication.getWriter().getNickname() + "님께서 " + careApplication.getCarePost().getName() + "(이)의 " + requestType + " 신청하셨습니다.")
                    .build();
        }
    }

    private Notification createNotification(CarePostComment carePostComment) {

        return Notification.builder()
                .contents(carePostComment.getWriter().getNickname() + "님이 회원님의 게시글에 댓글을 남겼습니다.")
                .targetIdx(carePostComment.getCarePost().getIdx())
                .targetType(RequestType.CAREPOST)
                .build();
    }

    private Notification createNotification(FundingComment fundingComment) {

        return Notification.builder()
                .contents(fundingComment.getWriter().getNickname() + "님이 회원님의 게시글에 댓글을 남겼습니다.")
                .targetIdx(fundingComment.getFunding().getIdx())
                .targetType(RequestType.FUNDING)
                .build();

    }

}