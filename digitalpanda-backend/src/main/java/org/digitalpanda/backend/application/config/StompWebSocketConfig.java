package org.digitalpanda.backend.application.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@EnableScheduling
public class StompWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    public static final String BACKEND_WS_STOMP_URL_OUTPUT_PREFIX = "/ws/stomp/backend-output";

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/stomp/handshake")
                .setAllowedOrigins("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/ws/stomp/frontend-input");
        registry.enableSimpleBroker(BACKEND_WS_STOMP_URL_OUTPUT_PREFIX);
    }

}