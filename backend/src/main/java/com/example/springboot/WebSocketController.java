package com.example.springboot;

import com.example.springboot.models.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebSocketController {

    @MessageMapping("/greeting")
    public String greeting(String username) {
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
