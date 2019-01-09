package com.sopt.rescat.service;

import com.amazonaws.services.s3.model.JSONOutput;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.common.base.Utf8;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.sopt.rescat.domain.Notification;
import com.sopt.rescat.domain.User;
import com.sopt.rescat.domain.UserNotificationLog;
import com.sopt.rescat.domain.enums.RequestStatus;
import com.sopt.rescat.exception.NotMatchException;
import com.sopt.rescat.repository.NotificationRepository;
import com.sopt.rescat.repository.UserNotificationLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

@Slf4j
@Service
public class NotificationService {

    private static final String PROJECT_ID = "rescat";
    private static final String BASE_URL = "https://fcm.googleapis.com";
    private static final String FCM_SEND_ENDPOINT = "/v1/projects/" + PROJECT_ID + "/messages:send";

    private static final String MESSAGING_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
    private static final String[] SCOPES = {MESSAGING_SCOPE };

//    private static final String TITLE = "FCM Notification";
//    private static final String BODY = "Notification from FCM";
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
                .fromStream(new FileInputStream("C:\\Users\\BYE\\Downloads\\rescat-firebase-adminsdk-v7wju-71936ecc50.json"))
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

//    public void writePush(Notification savedNotification, User receivingUser) {
//        send(NotificationDto.builder()
//                .body(savedNotification.getContents())
//                .to("68fc1bda1bd560ba66141f1820b68ee4ad2be6a59a52dbe0605ffb0c8f800c2c")
//                .build()
//                .toFormalNotification());
//    }

    @Transactional
    public void createNotification(User receivingUser, Notification notification){
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


        UserNotificationLog notificationLog =userNotificationLogRepository.findByNotificationAndReceivingUser(notification,user);

        if (notificationLog==null)
            throw new NotMatchException("idx", "해당 idx 알림은 사용자가 받은 알림이 아닙니다.");

        notificationLog.updateIsChecked();

        return notification;
    }
}