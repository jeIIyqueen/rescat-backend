package com.sopt.rescat.web.api;

import com.sopt.rescat.service.NotificationService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@Api(value = "FCMController", description = "알림 관련 api")
@RestController
@RequestMapping("/api/fcm")
public class ApiNotificaionController {

    private final String TOPIC = "sample";

    private final NotificationService notificationService;

    public ApiNotificaionController(final NotificationService notificationService){
        this.notificationService = notificationService;
    }


//    @PostMapping("")
//    public ResponseEntity getDeviceToken(@RequestBody String token){
//        pushNotificationsService.saveDeviceToken(token);
//        return ResponseEntity.status(HttpStatus.OK).build();
//    }

    @GetMapping("/send")
    public ResponseEntity<String> send() throws JSONException {

        JSONObject body = new JSONObject();

        body.put("to","/topics/"+TOPIC);
        body.put("priority","high");

//        //DB에 저장된 여러개의 토큰(수신자)을 가져와서 설정할 수 있다.//
//        List<String> tokenlist = new ArrayList<String>();
//        //DB과정 생략(직접 대입)//
//        tokenlist.add("token value 1");
//        tokenlist.add("token value 2");
//        tokenlist.add("token value 3");
//
//        JSONArray array = new JSONArray();
//
//        for(int i=0; i<tokenlist.size(); i++) {
//            array.put(tokenlist.get(i));
//        }
//
//        body.put("registration_ids", array); //여러개의 메시지일 경우 registration_ids, 단일 메세지는 to사용//

        JSONObject notification = new JSONObject();
        notification.put("title", "[Recat]");
        notification.put("body", "message");

        JSONObject data = new JSONObject();
        data.put("key-1","data1");
        data.put("key-2","data2");

        body.put("notification", notification);
        body.put("data",data);

        /**
         {
         "notification": {
         "title": "JSA Notification",
         "body": "Happy Message!"
         },
         "data": {
         "Key-1": "JSA Data 1",
         "Key-2": "JSA Data 2"
         },
         "to": "/topics/sample",
         "priority": "high"
         }
         */


        HttpEntity<String> request = new HttpEntity<>(body.toString());

        CompletableFuture<String> pushNotification = notificationService.send(request);
        CompletableFuture.allOf(pushNotification).join();

        try {
            String firebaseResponse = pushNotification.get();

            return new ResponseEntity<>(firebaseResponse, HttpStatus.OK);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>("Push Notification ERROR!", HttpStatus.BAD_REQUEST);
    }
}
