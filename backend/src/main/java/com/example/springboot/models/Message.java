package com.example.springboot.models;

public class Message {
    private final String userId;
    private final String message;

    public Message(String userId, String message) {
        this.userId = userId;
        this.message = message;
    }

    public String getUserId() {
        return userId;
    }

    public String getMessage() {
        return message;
    }
}
