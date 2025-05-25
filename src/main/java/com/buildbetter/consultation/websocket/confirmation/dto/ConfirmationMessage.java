package com.buildbetter.consultation.websocket.confirmation.dto;

import java.time.Instant;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ConfirmationMessage {
    String type;
    String consultationId;
    String message;
    Instant timestamp;
}
