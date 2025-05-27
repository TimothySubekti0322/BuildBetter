package com.buildbetter.consultation.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.buildbetter.consultation.dto.chat.UploadFileMessage;
import com.buildbetter.consultation.model.Chat;
import com.buildbetter.consultation.model.Room;
import com.buildbetter.consultation.repository.ChatRepository;
import com.buildbetter.consultation.repository.RoomRepository;
import com.buildbetter.shared.constant.S3Folder;
import com.buildbetter.shared.util.S3Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {
    private final RoomRepository roomRepository;
    private final ChatRepository chatRepository;
    private final S3Service s3Service;

    public List<Chat> getChatHistory(UUID roomId, UUID userId) {
        log.info("Fetching chat history for room: {}", roomId);

        // Check Access
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        if (!room.getUserId().equals(userId) && !room.getArchitectId().equals(userId)) {
            log.error("User {} does not have access to room {}", userId, roomId);
            throw new IllegalArgumentException("User does not have access to read chat history of this room");
        }

        List<Chat> history = chatRepository.findByRoomIdOrderByCreatedAtAsc(roomId);
        return history;
    }

    public String uploadFileMessage(UUID roomId, UUID userId, UploadFileMessage uploadFileMessage) {
        log.info("Uploading file message for room: {}", roomId);

        String folder = S3Folder.CHAT + roomId.toString() + "/";

        String fileUrl = s3Service.uploadFile(uploadFileMessage.getFile(), folder, userId.toString());

        return fileUrl;
    }
}
