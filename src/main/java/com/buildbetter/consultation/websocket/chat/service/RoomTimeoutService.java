// src/main/java/com/buildbetter/consultation/websocket/chat/service/RoomTimeoutService.java
package com.buildbetter.consultation.websocket.chat.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Added for status update
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import com.buildbetter.consultation.constant.ConsultationStatus;
import com.buildbetter.consultation.model.Consultation; // Added
import com.buildbetter.consultation.model.Room;
import com.buildbetter.consultation.repository.ConsultationRepository; // Added
import com.buildbetter.consultation.repository.RoomRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
// Ensure RoomRepository and ConsultationRepository are included in
// @RequiredArgsConstructor
// or add them to the constructor manually.
@RequiredArgsConstructor
public class RoomTimeoutService {

    private final TaskScheduler taskScheduler;
    private final ChatSessionManager sessionManager;
    private final RoomRepository roomRepository;
    private final ConsultationRepository consultationRepository; // Added

    private final ConcurrentHashMap<UUID, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    public void scheduleRoomTimeout(UUID roomId) {
        if (scheduledTasks.containsKey(roomId)) {
            log.debug("Timeout already scheduled for room {}", roomId);
            return;
        }

        try {
            Room room = roomRepository.findById(roomId).orElse(null);
            if (room == null) {
                log.warn("Cannot schedule timeout - Room {} not found", roomId);
                return;
            }

            LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Jakarta"));
            LocalDateTime endTime = room.getEndTime();

            if (now.isAfter(endTime)) {
                log.warn("Room {} has already expired (endTime: {}), processing timeout actions immediately.",
                        roomId, endTime);
                // Process immediately: disconnect users and update consultation
                executeRoomTimeoutActions(roomId, room, "Session has already expired");
                return;
            }

            Duration delay = Duration.between(now, endTime);
            long delayMillis = delay.toMillis();

            log.info("Scheduling timeout for room {} in {} ms (endTime: {})",
                    roomId, delayMillis, endTime);

            ScheduledFuture<?> scheduledTask = taskScheduler.schedule(
                    () -> executeRoomTimeout(roomId), // Lambda calls the existing method name
                    java.time.Instant.now().plusMillis(delayMillis));

            scheduledTasks.put(roomId, scheduledTask);

        } catch (Exception e) {
            log.error("Error scheduling timeout for room {}: {}", roomId, e.getMessage(), e);
        }
    }

    /**
     * Main method called by the scheduler when a room's time is up.
     */
    private void executeRoomTimeout(UUID roomId) {
        Room room = roomRepository.findById(roomId).orElse(null);
        if (room == null) {
            log.warn("Room {} not found at the time of scheduled timeout. Cannot process.", roomId);
            scheduledTasks.remove(roomId); // Clean up the task
            return;
        }
        // Call the consolidated action method
        executeRoomTimeoutActions(roomId, room, "Session expired");
    }

    /**
     * Consolidates actions to be taken when a room timeout occurs:
     * 1. Terminate WebSocket sessions.
     * 2. Update associated Consultation status.
     * 3. Clean up scheduled task.
     */
    private void executeRoomTimeoutActions(UUID roomId, Room room, String closeReason) {
        try {
            log.info("Executing timeout actions for room {} (reason: {})", roomId, closeReason);

            // 1. Terminate all sessions in the room
            terminateRoomSessions(roomId, closeReason);

            // 2. Update associated Consultation(s) status to "ENDED"
            updateAssociatedConsultationsToEnd(room);

            // 3. Remove the scheduled task from our tracking map (if it was regularly
            // scheduled)
            // If called directly for an already expired room, it might not be in
            // scheduledTasks.
            scheduledTasks.remove(roomId);

        } catch (Exception e) {
            log.error("Error executing timeout actions for room {}: {}", roomId, e.getMessage(), e);
            // Ensure task is removed even if there's an error in actions
            scheduledTasks.remove(roomId);
        }
    }

    /**
     * Updates the status of Consultations associated with the given Room to
     * "ENDED".
     * This method should be called when the room's session time (room.endTime) is
     * reached.
     */
    @Transactional // Make this operation transactional
    protected void updateAssociatedConsultationsToEnd(Room room) {
        UUID roomId = room.getId();
        LocalDateTime roomEndTime = room.getEndTime(); // This is the definitive end time

        // Find consultations that were using this room and scheduled to end at this
        // time
        Optional<Consultation> consultationsToEnd = consultationRepository.findByRoomIdAndEndDate(roomId, roomEndTime);

        if (consultationsToEnd.isEmpty()) {
            log.warn("No consultation found for room {} with end date {} to mark as {}.",
                    roomId, roomEndTime, ConsultationStatus.ENDED.getStatus());
            return;
        } else if (!ConsultationStatus.ENDED.getStatus().equalsIgnoreCase(consultationsToEnd.get().getStatus())) {
            consultationsToEnd.get().setStatus(ConsultationStatus.ENDED.getStatus());
            // You could also set an actual end time if needed, e.g.,
            // consultation.setActualEndTime(LocalDateTime.now());
            consultationRepository.save(consultationsToEnd.get());
            log.info("Consultation {} (scheduled end: {}) for room {} has been marked as {}.",
                    consultationsToEnd.get().getId(), roomEndTime, roomId, ConsultationStatus.ENDED.getStatus());
        } else {
            log.info("Consultation {} (scheduled end: {}) for room {} was already {}.",
                    consultationsToEnd.get().getId(), roomEndTime, roomId, ConsultationStatus.ENDED.getStatus());
        }
    }

    private void terminateRoomSessions(UUID roomId, String reason) {
        Set<WebSocketSession> sessions = sessionManager.getSessions(roomId);
        if (sessions.isEmpty()) {
            log.debug("No sessions to terminate for room {}", roomId);
            return;
        }
        log.info("Terminating {} session(s) for room {} - Reason: {}", sessions.size(), roomId, reason);
        for (WebSocketSession session : sessions) {
            try {
                if (session.isOpen()) {
                    session.close(new CloseStatus(4004, reason)); // 4004: Custom for session timeout
                    log.debug("Terminated WebSocket session {} for room {}", session.getId(), roomId);
                }
            } catch (Exception e) {
                log.error("Error closing WebSocket session {} for room {}: {}",
                        session.getId(), roomId, e.getMessage(), e);
            }
        }
    }

    public boolean cancelRoomTimeout(UUID roomId) {
        ScheduledFuture<?> scheduledTask = scheduledTasks.remove(roomId);
        if (scheduledTask != null && !scheduledTask.isDone()) {
            boolean cancelled = scheduledTask.cancel(false); // false: don't interrupt if already running
            log.info("Cancelled timeout for room {}: {}", roomId, cancelled);
            return cancelled;
        }
        return false;
    }

    public boolean hasScheduledTimeout(UUID roomId) {
        ScheduledFuture<?> task = scheduledTasks.get(roomId);
        return task != null && !task.isDone();
    }

    public int getScheduledTimeoutCount() {
        return (int) scheduledTasks.values().stream()
                .filter(task -> !task.isDone())
                .count();
    }

    public void cleanupCompletedTasks() {
        scheduledTasks.entrySet().removeIf(entry -> entry.getValue().isDone());
        log.debug("Cleaned up completed timeout tasks");
    }
}