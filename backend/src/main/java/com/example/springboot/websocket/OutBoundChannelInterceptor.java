package com.example.springboot.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OutBoundChannelInterceptor implements ChannelInterceptor {
    private static final String TAG = "OutBoundChannelInterceptor";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        logger.info(TAG + " - preSend");

        final StompHeaderAccessor headerAccessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (headerAccessor != null) {
            Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
            if (sessionAttributes != null && sessionAttributes.containsKey("customSessionId")) {
                String sessionId = sessionAttributes.get("customSessionId").toString();
                logger.info(TAG + " - customSessionId: " + sessionId);
            } else {
                logger.info(TAG + " - sessionAttributes is null");
            }
        } else {
            logger.info(TAG + " - headerAccessor is null");
        }

        return message;
    }

    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
        logger.info(TAG + " - afterSendCompletion");
    }
}
