package com.example.springboot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // Reference:
    // - https://spring.io/guides/gs/messaging-stomp-websocket/
    // - https://docs.spring.io/spring/docs/current/spring-framework-reference/web.html#websocket-intro

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/chat", "/greeting")
//                .setHeartbeatValue(new long[]{0, 1000})
                .setTaskScheduler(heartBeatScheduler());
        config.setApplicationDestinationPrefixes("/topic");
    }

    @Bean
    public TaskScheduler heartBeatScheduler() {
        // https://stackoverflow.com/questions/39220647/spring-stomp-over-websockets-not-scheduling-heartbeats
        return new ThreadPoolTaskScheduler();
    }
}
