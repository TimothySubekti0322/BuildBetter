package com.buildbetter.consultation.websocket.chat.service;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.buildbetter.consultation.model.Chat;
import com.buildbetter.consultation.repository.ChatRepository;
import com.buildbetter.consultation.websocket.chat.dto.ChatMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketService {

    private final ChatRepository chatRepository;

    @Transactional
    public ChatMessage persist(ChatMessage inbound) {

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Jakarta"));

        Chat entity = Chat.builder()
                .roomId(inbound.getRoomId())
                .sender(inbound.getSender())
                .senderRole(inbound.getSenderRole())
                .content(inbound.getContent())
                .type(inbound.getType())
                .createdAt(inbound.getSentAt() != null ? inbound.getSentAt() : now)
                .build();

        chatRepository.save(entity);

        log.info("ChatWebSocketService: persist - Chat message saved: {}", entity);

        if (inbound.getSentAt() == null) {
            inbound.setSentAt(now);
        }
        return inbound;
    }
}
