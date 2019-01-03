package com.sopt.rescat.service;

import com.sopt.rescat.domain.Notification;
import com.sopt.rescat.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class NotificationService {

    @Value("${FCM.SERVERKEY}")
    private String FIREBASE_SERVER_KEY;

    @Value("${FCM.APIURL}")
    private String FIREBASE_API_URL;

    @Async
    public CompletableFuture<String> send(HttpEntity<String> entity) {

        RestTemplate restTemplate = new RestTemplate();

        /**
         https://fcm.googleapis.com/fcm/send
         Content-Type:application/json
         Authorization:key=FIREBASE_SERVER_KEY*/

        ArrayList<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();

        interceptors.add(new HeaderRequestInterceptor("Authorization", "key=" + FIREBASE_SERVER_KEY));
        interceptors.add(new HeaderRequestInterceptor("Content-Type", "application/json"));
        restTemplate.setInterceptors(interceptors);

        String firebaseResponse = restTemplate.postForObject(FIREBASE_API_URL, entity, String.class);

        return CompletableFuture.completedFuture(firebaseResponse);
    }

    public void writePush(Notification savedNotification){
        JSONObject body = new JSONObject();

        body.put("to",savedNotification.getReceivingUser().getDeviceTokrn());
       // body.put("priority","high");

        JSONObject notification = new JSONObject();
   //     notification.put("title", savedNotification.getTitle());
        notification.put("body", savedNotification.getReceivingUser().getNickname()+savedNotification.getContents());

//        JSONObject data = new JSONObject();
//        data.put("key-1","data1");

        body.put("notification", notification);
//        body.put("data",data);

        HttpEntity<String> request = new HttpEntity<>(body.toString());
        send(request);
    }
}
