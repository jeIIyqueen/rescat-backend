package com.sopt.rescat.service;

import com.sopt.rescat.domain.Notification;
import com.sopt.rescat.domain.User;
import com.sopt.rescat.dto.NotificationDto;
import com.sopt.rescat.repository.NotificationRepository;
import jdk.nashorn.internal.objects.NativeObject;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class NotificationService {

    private NotificationRepository notificationRepository;

    @Value("${FCM.SERVERKEY}")
    private String FIREBASE_SERVER_KEY;

    @Value("${FCM.APIURL}")
    private String FIREBASE_API_URL;

    public NotificationService(final NotificationRepository notificationRepository) {
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

    public void writePush(Notification savedNotification) {
        send(NotificationDto.builder()
                .body(savedNotification.getContents())
                .to(savedNotification.getReceivingUser().toString())
                .build()
                .toFormalNotification());
    }
//
//    public List<Notification> getNotification(User user){
//
//        List<Notification> userNotifications = notificationRepository.findByReceivingUserOrderByCreatedAtDesc(user);
//
//        userNotifications.stream().filter(UserN)
//
//        return  ;
//    }
}