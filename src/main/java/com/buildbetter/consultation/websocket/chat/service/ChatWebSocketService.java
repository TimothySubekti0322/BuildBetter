package com.buildbetter.consultation.websocket.chat.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.buildbetter.consultation.model.Chat;
import com.buildbetter.consultation.repository.ChatRepository;
import com.buildbetter.consultation.websocket.chat.dto.ChatMessage;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatWebSocketService {

    private final ChatRepository chatRepository;

    @Transactional
    public ChatMessage persist(ChatMessage inbound) {

        LocalDateTime now = LocalDateTime.now();

        Chat entity = Chat.builder()
                .roomId(inbound.getRoomId())
                .sender(inbound.getSender())
                .content(inbound.getContent())
                .type(inbound.getType())
                .createdAt(now)
                .build();

        chatRepository.save(entity);

        inbound.setSentAt(now);
        return inbound;
    }
}
