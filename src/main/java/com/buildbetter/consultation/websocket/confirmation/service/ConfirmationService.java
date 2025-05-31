package com.buildbetter.consultation.websocket.confirmation.service;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.buildbetter.consultation.constant.CancellationReason;
import com.buildbetter.consultation.websocket.confirmation.dto.ConfirmationMessage;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfirmationService {
    private final ConfirmationSessionManager confirmationSessionManager;
    private final ObjectMapper objectMapper;

    public void notifyApproved(String consultationId) {

        log.info("ConfirmationService (Websocket) : notifyApproved - Broadcasting approval for consultation {}",
                consultationId);

        try {
            String payload = objectMapper.writeValueAsString(
                    ConfirmationMessage.builder()
                            .type("APPROVED")
                            .consultationId(consultationId)
                            .message("Your consultation has been approved")
                            .timestamp(Instant.now())
                            .build());

            log.info("ConfirmationService (Websocket) : notifyApproved - Payload: {}", payload);
            confirmationSessionManager.broadcastWaiting(consultationId, payload);
        } catch (Exception e) {
            log.error("Error broadcasting approval", e);
        }
    }

    public void notifyRejected(String consultationId, CancellationReason reason) {
        log.info("ConfirmationService (Websocket) : notifyRejected - Broadcasting rejection for consultation {}",
                consultationId);
        try {
            String payload = objectMapper.writeValueAsString(
                    ConfirmationMessage.builder()
                            .type("REJECTED")
                            .consultationId(consultationId)
                            .message(reason.getReason())
                            .timestamp(Instant.now())
                            .build());

            log.info("ConfirmationService (Websocket) : notifyRejected - Payload: {}", payload);
            confirmationSessionManager.broadcastWaiting(consultationId, payload);
        } catch (Exception e) {
            log.error("Error broadcasting approval", e);
        }
    }
}
