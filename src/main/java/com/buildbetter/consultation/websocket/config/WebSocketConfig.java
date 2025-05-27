package com.buildbetter.consultation.websocket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.buildbetter.consultation.websocket.chat.handler.ChatWebSocketHandler;
import com.buildbetter.consultation.websocket.chat.security.ChatAuthHandshakeInterceptor;
import com.buildbetter.consultation.websocket.confirmation.handler.ConfirmationWebSocketHandler;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    // private final ChatWebSocketHandler chatWebSocketHandler;
    private final ConfirmationWebSocketHandler confirmationWebSocketHandler;
    private final ChatWebSocketHandler chatWebSocketHandler;

    private final ChatAuthHandshakeInterceptor chatAuth;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/ws/chat/{roomId}")
                .addInterceptors(chatAuth)
                .setAllowedOrigins("*");

        registry.addHandler(confirmationWebSocketHandler, "/ws/waiting-confirmation/{consultationId}")
                .setAllowedOrigins("*"); // Configure this properly for production
    }
}