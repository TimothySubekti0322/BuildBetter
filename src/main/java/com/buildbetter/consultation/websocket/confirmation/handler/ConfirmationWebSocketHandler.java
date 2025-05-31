package com.buildbetter.consultation.websocket.confirmation.handler;

import java.net.URI;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.buildbetter.consultation.websocket.confirmation.service.ConfirmationSessionManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConfirmationWebSocketHandler extends TextWebSocketHandler {

    private final ConfirmationSessionManager confirmationSessionManager;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info(
                "ConfirmationWebSocketHandler (Websocket) : afterConnectionEstablished - User connected to waiting WS for consultation {}",
                session.getId());

        String consultationId = extractId(session);
        confirmationSessionManager.addWaitingSession(consultationId, session);

        log.info("User connected to waiting WS for consultation {}", consultationId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info(
                "ConfirmationWebSocketHandler (Websocket) : afterConnectionClosed - User disconnected from waiting WS for consultation {}",
                session.getId());

        String consultationId = extractId(session);
        confirmationSessionManager.removeWaitingSession(consultationId, session);

        log.info("User disconnected from waiting WS {}", consultationId);
    }

    private String extractId(WebSocketSession session) {
        log.info(
                "ConfirmationWebSocketHandler (Websocket) : extractId - Extracting consultation ID from session URI: {}",
                session.getUri());

        URI uri = session.getUri();

        return uri.getPath().substring(uri.getPath().lastIndexOf('/') + 1);
    }
}
