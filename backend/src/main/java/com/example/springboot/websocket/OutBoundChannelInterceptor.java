package com.example.springboot.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import java.util.Map;

@Component
public class OutBoundChannelInterceptor implements ChannelInterceptor {
    private static final String TAG = "OutBoundChannelInterceptor";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        logger.info(TAG + " - preSend");

        final StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);
        final StompCommand command = headerAccessor.getCommand() != null ? headerAccessor.getCommand() : StompCommand.ACK;
        logger.info(TAG + " - command: " + command);
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        if (sessionAttributes != null && sessionAttributes.containsKey("customSessionId")) {
            String sessionId = sessionAttributes.get("customSessionId").toString();
            logger.info(TAG + " - customSessionId: " + sessionId);

            final StompHeaderAccessor newHeaderAccessor = StompHeaderAccessor.create(command);
            newHeaderAccessor.setSessionId(headerAccessor.getSessionId());
            @SuppressWarnings("unchecked")
            final MultiValueMap<String, String> nativeHeaders = (MultiValueMap<String, String>) headerAccessor.getHeader(StompHeaderAccessor.NATIVE_HEADERS);
            newHeaderAccessor.addNativeHeaders(nativeHeaders);

            // add custom headers
            newHeaderAccessor.addNativeHeader("x-server-timestamp", String.valueOf(System.currentTimeMillis()));
            return MessageBuilder.createMessage(new byte[0], newHeaderAccessor.getMessageHeaders());
        } else {
            logger.info(TAG + " - sessionAttributes is null");
        }

        return message;
    }

    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
        logger.info(TAG + " - afterSendCompletion");
    }
}
