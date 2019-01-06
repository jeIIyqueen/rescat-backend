package com.sopt.rescat.service;

import com.sopt.rescat.domain.Notification;
import com.sopt.rescat.domain.User;
import com.sopt.rescat.domain.UserNotificationLog;
import com.sopt.rescat.domain.enums.RequestType;
import com.sopt.rescat.dto.NotificationDto;
import com.sopt.rescat.exception.NotMatchException;
import com.sopt.rescat.repository.NotificationRepository;
import com.sopt.rescat.repository.UserNotificationLogRepository;
import jdk.nashorn.internal.objects.NativeObject;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NotificationService {

    private UserNotificationLogRepository userNotificationLogRepository;
    private NotificationRepository notificationRepository;

    @Value("${FCM.SERVERKEY}")
    private String FIREBASE_SERVER_KEY;

    @Value("${FCM.APIURL}")
    private String FIREBASE_API_URL;

    public NotificationService(final UserNotificationLogRepository userNotificationLogRepository,
                               final NotificationRepository notificationRepository) {
        this.userNotificationLogRepository = userNotificationLogRepository;
        this.notificationRepository = notificationRepository;
    }

    /**
     * @param notificationBody = {
     *                         "notification": {
     *                         "title": "JSA Notification",
     *                         "body": "Happy Message!"
     *                         },
     *                         "data": {
     *                         "Key-1": "JSA Data 1",
     *                         "Key-2": "JSA Data 2"
     *                         },
     *                         "to": "/topics/sample",
     *                         "priority": "high"
     *                         }
     * @return
     */
    @Async
    public CompletableFuture<String> send(HttpEntity<String> notificationBody) {
        RestTemplate restTemplate = new RestTemplate();
        ArrayList<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();

        interceptors.add(new HeaderRequestInterceptor("Authorization", "key=" + FIREBASE_SERVER_KEY));
        interceptors.add(new HeaderRequestInterceptor("Content-Type", "application/json"));
        restTemplate.setInterceptors(interceptors);

        String firebaseResponse = restTemplate.postForObject(FIREBASE_API_URL, notificationBody, String.class);

        return CompletableFuture.completedFuture(firebaseResponse);
    }

    public void writePush(Notification savedNotification, User receivingUser) {
        send(NotificationDto.builder()
                .body(savedNotification.getContents())
                .to("05b4a1e9e0fc295538bf8bb312da60119120bc34ab735321bf76a283f8e544aa")
                .build()
                .toFormalNotification());
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

        userNotificationLogRepository.findByNotificationAndReceivingUser(notification,user).updateIsChecked();

        return notification;
    }
}