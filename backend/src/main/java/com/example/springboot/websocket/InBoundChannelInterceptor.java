package com.example.springboot.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

import java.util.Map;

public class InBoundChannelInterceptor implements ChannelInterceptor {

    private static final String TAG = "InBoundChannelInterceptor";
    private final Logger logger = LoggerFactory.getLogger(TAG);

    @Override
    public boolean preReceive(MessageChannel channel) {
        logger.info(this.getClass().getCanonicalName() + "preReceive");
        return true;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        final StompHeaderAccessor headerAccessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (headerAccessor != null) {
            Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
            if (sessionAttributes != null && sessionAttributes.containsKey("customSessionId")) {
                String sessionId = sessionAttributes.get("customSessionId").toString();
                logger.info(TAG + " - customSessionId: " + sessionId);
            } else {
                logger.info(TAG + " - sessionAttributes is null");
            }

            StompCommand stompCommand = headerAccessor.getCommand();
            String sessionId = headerAccessor.getSessionId();
            logger.info(TAG + " - stompCommand: " + stompCommand + ", sessionId: " + sessionId);
        } else {
            logger.info(TAG + " - headerAccessor is null");
        }

        return message;
    }

    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand stompCommand = accessor.getCommand();
        logger.info(TAG + " - stompCommand: " + stompCommand);
    }
}
