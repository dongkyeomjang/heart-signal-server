package com.heartsignal.dev.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/subscribe");  //메세지 브로커가 메시지를 뿌릴때
        registry.setApplicationDestinationPrefixes("/app"); //클라에서 메세지 브로커한테 메시지를 보낼때
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-connection")//처음 핸드쉐이킹
                /**
                 * TODO 도메인 설정 바꾸기
                 */
                .setAllowedOrigins("http://localhost:3000")
                .setAllowedOrigins("http://localhost:5173")
                .setAllowedOrigins("http://43.202.145.101:8080")
                .withSockJS();
    }
}