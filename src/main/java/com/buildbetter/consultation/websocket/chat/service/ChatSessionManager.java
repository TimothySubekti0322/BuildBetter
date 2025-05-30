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

    /**
     * Get all room IDs that currently have active sessions
     * Used by the timeout service to check for expired rooms
     */
    public Set<UUID> getActiveRoomIds() {
        return sessions.keySet();
    }

    /**
     * Get total number of active sessions across all rooms
     * Useful for monitoring
     */
    public int getTotalActiveSessions() {
        return sessions.values().stream()
                .mapToInt(Set::size)
                .sum();
    }

    /**
     * Get number of active sessions for a specific room
     */
    public int getSessionCount(UUID roomId) {
        return sessions.getOrDefault(roomId, Set.of()).size();
    }

    /**
     * Check if a room has any active sessions
     */
    public boolean hasActiveSessions(UUID roomId) {
        Set<WebSocketSession> roomSessions = sessions.get(roomId);
        return roomSessions != null && !roomSessions.isEmpty();
    }

    /**
     * Check if adding this session would make it the first participant
     * This method should be called BEFORE registering the session
     */
    public boolean wouldBeFirstParticipant(UUID roomId) {
        return !hasActiveSessions(roomId);
    }
}