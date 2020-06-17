package com.example.springboot.models;

public class Message {
    private final String userId;
    private final String Content;

    public Message(String userId, String content) {
        this.userId = userId;
        Content = content;
    }

    public String getUserId() {
        return userId;
    }

    public String getContent() {
        return Content;
    }
}
