package com.buildbetter.consultation.websocket.confirmation.service;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ConfirmationSessionManager {
    private final ConcurrentHashMap<String, CopyOnWriteArraySet<WebSocketSession>> confirmationSessions = new ConcurrentHashMap<>();

    public void addWaitingSession(String consultationId, WebSocketSession session) {
        log.info(
                "ConfirmationSessionManager (Websocket) : addWaitingSession - Adding session for consultationId: {}",
                consultationId);

        confirmationSessions
                .computeIfAbsent(consultationId, id -> new CopyOnWriteArraySet<>())
                .add(session);
    }

    public void removeWaitingSession(String consultationId, WebSocketSession session) {
        log.info(
                "ConfirmationSessionManager (Websocket) : removeWaitingSession - Removing session for consultationId: {}",
                consultationId);
        var set = confirmationSessions.get(consultationId);
        if (set != null) {
            set.remove(session);
            if (set.isEmpty())
                confirmationSessions.remove(consultationId);
        }
    }

    public void broadcastWaiting(String consultationId, String payload) {
        log.info(
                "ConfirmationSessionManager (Websocket) : broadcastWaiting - Broadcasting waiting message for consultationId: {}",
                consultationId);
        var set = confirmationSessions.get(consultationId);
        if (set != null) {
            for (WebSocketSession ws : set) {
                try {
                    if (ws.isOpen()) {
                        ws.sendMessage(new TextMessage(payload));
                    }
                } catch (IOException e) {
                    log.error("Failed to send waiting message", e);
                    set.remove(ws);
                }
            }
        }
        log.info(
                "ConfirmationSessionManager (Websocket) : broadcastWaiting - Finished broadcasting Confirmation message for consultationId: {}",
                consultationId);
    }

    // Utility methods
    public int getConfirmationSessionCount(String consultationId) {
        CopyOnWriteArraySet<WebSocketSession> sessions = confirmationSessions.get(consultationId);
        return sessions != null ? sessions.size() : 0;
    }
}
