package com.buildbetter.consultation.websocket.chat.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private UUID roomId;
    private UUID sender; // architectId OR userId
    private String senderRole; // architect or user role
    private String content; // plain text for now
    private String type; // e.g. "TEXT", "FILE" – keep flexible
    private LocalDateTime sentAt; // filled by server
}
