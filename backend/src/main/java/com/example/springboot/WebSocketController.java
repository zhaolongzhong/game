package com.example.springboot;

import com.example.springboot.models.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class WebSocketController {
    private static final String TAG = "WebSocketController";

    @MessageMapping("/greeting")
    public String greeting(String username, SimpMessageHeaderAccessor headerAccessor) {

        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        if (sessionAttributes != null && sessionAttributes.containsKey("sessionId")) {
            String sessionId = sessionAttributes.get("sessionId").toString();
            System.out.println(TAG + " - sessionId: " + sessionId);
        }

        String response =  "hello, " + username;
        System.out.println("greeting response: " + response);
        return response;
    }

    @MessageMapping("/chat")
    public String chat(Message payload) {

        String response =  "hello " + payload.getUserId();
        System.out.println("chat response:" + response);
        return response;
    }
}
