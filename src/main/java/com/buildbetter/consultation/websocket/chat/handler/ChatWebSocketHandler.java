package com.buildbetter.consultation.websocket.chat.handler;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.buildbetter.consultation.constant.ConsultationStatus;
import com.buildbetter.consultation.model.Consultation;
import com.buildbetter.consultation.model.Room;
import com.buildbetter.consultation.repository.ConsultationRepository;
import com.buildbetter.consultation.repository.RoomRepository;
import com.buildbetter.consultation.websocket.chat.dto.ChatMessage;
import com.buildbetter.consultation.websocket.chat.service.ChatSessionManager;
import com.buildbetter.consultation.websocket.chat.service.ChatWebSocketService;
import com.buildbetter.consultation.websocket.chat.service.RoomTimeoutService;
import com.buildbetter.shared.exception.BadRequestException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper mapper;
    private final ChatWebSocketService chatWebSocketService;
    private final ChatSessionManager sessionManager;
    private final RoomTimeoutService roomTimeoutService;
    private final ConsultationRepository consultationRepository;
    private final RoomRepository roomRepository;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String reason = (String) session.getAttributes().get("rejectionReason");
        if (reason != null) {
            session.close(new CloseStatus(4003, reason));
            return;
        }

        UUID roomId = (UUID) session.getAttributes().get("roomId");
        UUID userId = (UUID) session.getAttributes().get("userId");

        // Check if this is the first participant in the room
        boolean isFirstParticipant = !sessionManager.hasActiveSessions(roomId);

        // Register the session
        sessionManager.register(roomId, session);

        // If this is the first participant, schedule the room timeout
        if (isFirstParticipant) {
            log.info("First participant connected to room {}, scheduling timeout", roomId);
            roomTimeoutService.scheduleRoomTimeout(roomId);

            // Update Room status to In-Progress
            log.info("Updating room status to IN_PROGRESS for room {}", roomId);
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new BadRequestException("Room not found: " + roomId));

            Consultation consultation = consultationRepository.findByRoomIdAndEndDate(roomId, room.getEndTime())
                    .orElseThrow(() -> new BadRequestException("Consultation not found for room: " + roomId));

            consultation.setStatus(ConsultationStatus.IN_PROGRESS.getStatus());
            consultationRepository.save(consultation);
        }

        log.info("WS connected: room={}, user={}, session={}, totalInRoom={}",
                roomId, userId, session.getId(), sessionManager.getSessionCount(roomId));
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage payload) throws Exception {
        UUID roomId = extractRoomId(session);

        ChatMessage inbound = mapper.readValue(payload.getPayload(), ChatMessage.class);
        inbound.setRoomId(roomId); // trust the path, not the client
        ChatMessage outbound = chatWebSocketService.persist(inbound);

        String json = mapper.writeValueAsString(outbound);
        TextMessage broadcast = new TextMessage(json.getBytes(StandardCharsets.UTF_8));

        for (WebSocketSession peer : sessionManager.getSessions(roomId)) {
            if (peer.isOpen())
                peer.sendMessage(broadcast);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        UUID roomId = extractRoomId(session);
        sessionManager.remove(roomId, session);

        // Check if this was the last participant
        boolean hasRemainingParticipants = sessionManager.hasActiveSessions(roomId);

        log.info("WS closed: room={}, session={}, status={}, remainingInRoom={}",
                roomId, session.getId(), status,
                hasRemainingParticipants ? sessionManager.getSessionCount(roomId) : 0);

        // Optional: Cancel timeout if no participants remain (saves resources)
        // Comment this out if you want the room to timeout even with no participants
        if (!hasRemainingParticipants) {
            boolean cancelled = roomTimeoutService.cancelRoomTimeout(roomId);
            if (cancelled) {
                log.info("Cancelled timeout for empty room {}", roomId);
            }
        }
    }

    /* ------------------------------------------------ */
    /* Helpers */
    /* ------------------------------------------------ */

    private UUID extractRoomId(WebSocketSession session) {
        // PathPattern "/ws/chat/{roomId}" ⇒ first path variable
        String roomStr = session.getUri().getPath().replaceFirst(".*/chat/", "");
        // strip leading slash in case it lingers
        roomStr = roomStr.startsWith("/") ? roomStr.substring(1) : roomStr;
        return UUID.fromString(roomStr);
    }
}