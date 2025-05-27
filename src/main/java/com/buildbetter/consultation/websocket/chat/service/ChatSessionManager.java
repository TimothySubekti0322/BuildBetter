package com.buildbetter.consultation.websocket.chat.service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
public class ChatSessionManager {

    // roomId → sessions
    private final Map<UUID, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();

    public void register(UUID roomId, WebSocketSession session) {
        sessions.computeIfAbsent(roomId, k -> new CopyOnWriteArraySet<>()).add(session);
    }

    public void remove(UUID roomId, WebSocketSession session) {
        Optional.ofNullable(sessions.get(roomId)).ifPresent(set -> {
            set.remove(session);
            if (set.isEmpty())
                sessions.remove(roomId);
        });
    }

    public Set<WebSocketSession> getSessions(UUID roomId) {
        return sessions.getOrDefault(roomId, Set.of());
    }
}