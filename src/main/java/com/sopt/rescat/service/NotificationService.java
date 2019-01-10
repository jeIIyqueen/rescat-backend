package com.sopt.rescat.service;

import com.sopt.rescat.domain.Notification;
import com.sopt.rescat.domain.User;
import com.sopt.rescat.domain.UserNotificationLog;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.sopt.rescat.domain.*;
import com.sopt.rescat.domain.enums.RequestStatus;
import com.sopt.rescat.domain.enums.RequestType;
import com.sopt.rescat.exception.InvalidValueException;
import com.sopt.rescat.exception.NotMatchException;
import com.sopt.rescat.repository.NotificationRepository;
import com.sopt.rescat.repository.UserNotificationLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

@Slf4j
@Service
public class NotificationService {
//
//    public static final String REFUSE_MESSAGE = "신청이 거절되었습니다. 별도의 문의사항은 마이페이지 > 문의하기 탭을 이용해주시기 바랍니다.";
//    public static final String APPROVE_MESSAGE = "신청이 승인되었습니다. 회원님의 목표금액 달성을 응원합니다.";
    private static final String PROJECT_ID = "rescat";
    private static final String BASE_URL = "https://fcm.googleapis.com";
    private static final String FCM_SEND_ENDPOINT = "/v1/projects/" + PROJECT_ID + "/messages:send";

    private static final String MESSAGING_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
    private static final String[] SCOPES = {MESSAGING_SCOPE };

    public static final String MESSAGE_KEY = "message";

    private UserNotificationLogRepository userNotificationLogRepository;
    private NotificationRepository notificationRepository;

    public NotificationService(final UserNotificationLogRepository userNotificationLogRepository,
                               final NotificationRepository notificationRepository) {
        this.userNotificationLogRepository = userNotificationLogRepository;
        this.notificationRepository = notificationRepository;
    }

    /**
     * Retrieve a valid access token that can be use to authorize requests to the FCM REST
     * API.
     *
     * @return Access token.
     * @throws IOException
     */
    // [START retrieve_access_token]
    private static String getAccessToken() throws IOException {
        GoogleCredential googleCredential = GoogleCredential
                .fromStream(new FileInputStream("src/main/resources/rescat-firebase-adminsdk-v7wju-c0635347b7.json"))
                .createScoped(Arrays.asList(SCOPES));
        googleCredential.refreshToken();
        return googleCredential.getAccessToken();
    }
    // [END retrieve_access_token]

    /**
     * Create HttpURLConnection that can be used for both retrieving and publishing.
     *
     * @return Base HttpURLConnection.
     * @throws IOException
     */
    private static HttpURLConnection getConnection() throws IOException {
        // [START use_access_token]
        URL url = new URL(BASE_URL + FCM_SEND_ENDPOINT);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestProperty("Authorization", "Bearer " + getAccessToken());
        httpURLConnection.setRequestProperty("Content-Type", "application/json");
        return httpURLConnection;
        // [END use_access_token]
    }

    /**
     * Send request to FCM message using HTTP.
     *
     * @param fcmMessage Body of the HTTP request.
     * @throws IOException
     */
    private static void sendPush(JsonObject fcmMessage) throws IOException {

        HttpURLConnection connection = getConnection();
        connection.setDoOutput(true);

        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(fcmMessage.toString().getBytes("UTF-8"));
        outputStream.flush();
        outputStream.close();

        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            String response = inputstreamToString(connection.getInputStream());
            System.out.println("Message sent to Firebase for delivery, response:");
            System.out.println(response);
        } else {
            System.out.println("Unable to send message to Firebase:");
            String response = inputstreamToString(connection.getErrorStream());
            System.out.println(response);
        }
    }

    /**
     * Send a message that uses the common FCM fields to send a notification message to all
     * platforms. Also platform specific overrides are used to customize how the message is
     * received on Android and iOS.
     *
     * @throws IOException
     */
    public static void writePush(String instanceToken, String body) throws IOException {
        JsonObject Message = buildOverrideMessage(instanceToken, body);
        System.out.println("FCM request body for override message:");
        prettyPrint(Message);
        sendPush(Message);
    }

    /**
     * Build the body of an FCM request. This body defines the common notification object
     * as well as platform specific customizations using the android and apns objects.
     *
     * @return JSON representation of the FCM request body.
     */
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

    /**
     * Build the android payload that will customize how a message is received on Android.
     *
     * @return android payload of an FCM request.
     */
    private static JsonObject buildAndroidOverridePayload() {
        JsonObject androidNotification = new JsonObject();
        androidNotification.addProperty("click_action", "android.intent.action.MAIN");

        JsonObject androidNotificationPayload = new JsonObject();
        androidNotificationPayload.add("notification", androidNotification);
        androidNotificationPayload.addProperty("priority","normal");

        return androidNotificationPayload;
    }

    /**
     * Build the apns payload that will customize how a message is received on iOS.
     *
     * @return apns payload of an FCM request.
     */
    private static JsonObject buildApnsHeadersOverridePayload() {
        JsonObject apnsHeaders = new JsonObject();
        apnsHeaders.addProperty("apns-priority", "5");

        return apnsHeaders;
    }

    /**
     * Build aps payload that will add a badge field to the message being sent to
     * iOS devices.
     *
     * @return JSON object with aps payload defined.
     */
    private static JsonObject buildApsOverridePayload() {
        JsonObject badgePayload = new JsonObject();
        badgePayload.addProperty("badge", 1);

        JsonObject apsPayload = new JsonObject();
        apsPayload.add("aps", badgePayload);

        return apsPayload;
    }

    /**
     * Construct the body of a notification message request.
     *
     * @return JSON of notification message.
     */
    private static JsonObject buildNotificationMessage(String instanceToken, String body) {
        JsonObject jNotification = new JsonObject();

        //jNotification.addProperty("title", "rescat");
        jNotification.addProperty("body", body);

        JsonObject jMessage = new JsonObject();
        jMessage.addProperty("token",instanceToken);
        jMessage.add("notification", jNotification);
  //      jMessage.addProperty("topic", "news");

        JsonObject jFcm = new JsonObject();
        jFcm.add(MESSAGE_KEY, jMessage);

        return jFcm;
    }

    /**
     * Read contents of InputStream into String.
     *
     * @param inputStream InputStream to read.
     * @return String containing contents of InputStream.
     * @throws IOException
     */
    private static String inputstreamToString(InputStream inputStream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        Scanner scanner = new Scanner(inputStream);
        while (scanner.hasNext()) {
            stringBuilder.append(scanner.nextLine());
        }
        return stringBuilder.toString();
    }

    /**
     * Pretty print a JsonObject.
     *
     * @param jsonObject JsonObject to pretty print.
     */
    private static void prettyPrint(JsonObject jsonObject) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println(gson.toJson(jsonObject) + "\n");
    }

    @Transactional
    public void pushNotification(User receivingUser, Notification notification){
        if(receivingUser.getInstanceToken()==null)
            return;
        userNotificationLogRepository.save(
                UserNotificationLog.builder()
                        .receivingUser(receivingUser)
                        .notification(notification)
                        .isChecked(RequestStatus.DEFER.getValue())
                        .build());
        try {
            writePush(receivingUser.getInstanceToken(),notification.getContents());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Transactional
    public List<UserNotificationLog> getNotification(User user) {

        List<UserNotificationLog> userNotificationLogs = userNotificationLogRepository.findByReceivingUserOrderByCreatedAtDesc(user);
        userNotificationLogs.stream()
                .filter(userNotificationLog -> userNotificationLog.getNotification().isTargetIdxNull())
                .forEach(userNotificationLog -> userNotificationLog.updateIsChecked());

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

//    public Notification createRefuseNotification(Funding funding) {
//        return Notification.builder()
//                .contents(funding.getWriter().getNickname() + "님의 후원글 " + REFUSE_MESSAGE)
//                .build();
//    }
//
//    public Notification createRefuseNotification(String writerName) {
//        return Notification.builder()
//                .contents(writerName + "님의 입양" + REFUSE_MESSAGE)
//                .build();
//    }
//
//    public Notification createApprovingNotification(Funding funding) {
//        return Notification.builder()
//                .contents(funding.getWriter().getNickname() + "님의 후원글 " + APPROVE_MESSAGE)
//                .targetType(RequestType.FUNDING)
//                .targetIdx(funding.getIdx())
//                .build();
//    }


    public<T> void send(T object, User receivingUser) {
        Notification notification;

        if(object instanceof Funding){
            notification = createNotification((Funding) object,receivingUser);
        }
        else if(object instanceof CarePost) {
            notification = createNotification((CarePost) object,receivingUser);
        }
        else if(object instanceof CareTakerRequest) {
            notification = createNotification((CareTakerRequest) object,receivingUser);
        }
        else if(object instanceof MapRequest) {
            notification = createNotification((MapRequest) object,receivingUser);
        }
        else if(object instanceof CareApplication){
            notification = createNotification((CareApplication)object,receivingUser);
        }
        else if(object instanceof CarePostComment){
            notification = createNotification((CarePostComment)object);
        }
        else if(object instanceof FundingComment){
            notification = createNotification((FundingComment)object);
        }
        else
            throw new InvalidValueException("notification", "알림값이 잘못 되었습니다.");

        notificationRepository.save(notification);

        // TODO 보내기
        pushNotification(receivingUser,notification);
    }


    // TODO method overloading 이용해서 메소드 여러개 만들기
    public Notification createNotification(Funding funding, User receivingUser) {

        if(funding.getIsConfirmed().equals(RequestStatus.CONFIRM)) {
            return Notification.builder()
                    .targetType(RequestType.FUNDING)
                    .targetIdx(funding.getIdx())
                    .contents(receivingUser.getNickname() + "님의 후원글 신청이 승인되었습니다. 회원님의 목표금액 달성을 응원합니다.")
                    .build();
        }
        return Notification.builder()
                .contents(receivingUser.getNickname() + "님의 후원글 신청이 거절되었습니다. 별도의 문의사항은 마이페이지 > 문의하기 탭을 이용해주시기 바랍니다.")
                .build();
    }

    public Notification createNotification(CarePost carePost, User receivingUser) {
        String requestType = (carePost.getType() == 0) ? "입양" : "임시보호";
        if(carePost.getIsConfirmed().equals(RequestStatus.CONFIRM)){
            return Notification.builder()
                    .targetType(RequestType.CAREPOST)
                    .targetIdx(carePost.getIdx())
                    .contents(receivingUser.getNickname() + "님의 "+requestType+" 등록 신청이 승인되었습니다. 좋은 "+requestType+"자를 만날 수 있기를 응원합니다.")
                    .build();
        }
        return Notification.builder()
                .contents(receivingUser.getNickname() + "님의 "+requestType+" 등록 신청이 거절되었습니다. 별도의 문의사항은 마이페이지 > 문의하기 탭을 이용해주시기 바랍니다.")
                .build();
    }

    public Notification createNotification(CareTakerRequest careTakerRequest, User receivingUser){
        //지역추가 or 케테 신청
        String requestType = (careTakerRequest.getType() == 0) ? "케어테이커" : "활동지역 추가";

        if(careTakerRequest.getIsConfirmed().equals(RequestStatus.CONFIRM))
            return Notification.builder()
                    .contents(receivingUser.getNickname() + "님의 "+requestType+" 신청이 승인되었습니다. 앞으로 활발한 활동 부탁드립니다.")
                    .build();
        return Notification.builder()
                .contents(receivingUser.getNickname() + "님의 "+requestType+" 신청이 거절되었습니다. 별도의 문의사항은 마이페이지 > 문의하기 탭을 이용해주시기 바랍니다.")
                .build();
    }

    public Notification createNotification(MapRequest mapRequest, User receivingUser) {
        String requestType = (mapRequest.getRequestType() == 0) ? "등록" : "수정";
        String registerType;
        if(mapRequest.getRegisterType() == 0)
            registerType = "배식소";
        else if (mapRequest.getRegisterType() == 1)
            registerType = "병원";
        else
            registerType = "고양이";

        if(mapRequest.getIsConfirmed().equals(RequestStatus.CONFIRM))
            return Notification.builder()
                    .contents(receivingUser.getNickname()+ "님의 " + registerType + requestType + " 요청이 거절되었습니다. 별도의 문의사항은 마이페이지 > 문의하기 탭을 이용해주시기 바랍니다.")
                    .build();
        return Notification.builder()
                .contents(receivingUser.getNickname()+ "님의 " + registerType + requestType + " 요청이 승인되었습니다.")
                .build();
    }

    public Notification createNotification(CareApplication careApplication, User receivingUser){
        String requestType = (careApplication.getCarePost().getType() == 0) ? "입양" : "임시보호";

        if(careApplication.getIsAccepted())
            return Notification.builder()
                    .contents(receivingUser.getNickname() + "님의 "+requestType+" 신청이 승인되었습니다. 당신의 아름다운 결정을 지지합니다.")
                    .build();

        requestType = (careApplication.getCarePost().getType() == 0) ? "입양을" : "임시보호를";
        return Notification.builder()
                .targetIdx(careApplication.getIdx())
                .targetType(RequestType.CAREAPPLICATION)
                .contents(receivingUser.getNickname() + "님께서 " + careApplication.getCarePost().getName() + "(이)의 " + requestType + " 신청하셨습니다.")
                .build();
    }

    private Notification createNotification(CarePostComment carePostComment){

        return Notification.builder()
                .contents(carePostComment.getWriter().getNickname() + "님이 회원님의 게시글에 댓글을 남겼습니다.")
                .targetIdx(carePostComment.getCarePost().getIdx())
                .targetType(RequestType.CAREPOST)
                .build();
    }

    private Notification createNotification(FundingComment fundingComment){

        return Notification.builder()
                .contents(fundingComment.getWriter().getNickname() + "님이 회원님의 게시글에 댓글을 남겼습니다.")
                .targetIdx(fundingComment.getFunding().getIdx())
                .targetType(RequestType.FUNDING)
                .build();

    }
}