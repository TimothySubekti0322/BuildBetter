package com.buildbetter.consultation.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.buildbetter.consultation.dto.chat.UploadFileMessage;
import com.buildbetter.consultation.model.Chat;
import com.buildbetter.consultation.service.ChatService;
import com.buildbetter.shared.dto.ApiResponseMessageAndData;
import com.buildbetter.shared.dto.ApiResponseWithData;
import com.buildbetter.shared.security.JwtAuthentication;
import com.buildbetter.shared.security.annotation.Authenticated;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chats")
@Slf4j
public class ChatController {
    private final ChatService chatService;

    @GetMapping("/{roomId}")
    @Authenticated
    public ApiResponseWithData<List<Chat>> getRoomChatHistory(Authentication auth, @PathVariable UUID roomId) {
        log.info("Chat Controller : getRoomChatHistory");

        log.info("Chat Controller : getRoomChatHistory - Parse JWT Authentication");
        JwtAuthentication jwt = (JwtAuthentication) auth;
        UUID userId = UUID.fromString(jwt.claim("id"));

        List<Chat> chatHistory = chatService.getChatHistory(roomId, userId);

        ApiResponseWithData<List<Chat>> response = new ApiResponseWithData<>();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setData(chatHistory);

        return response;
    }

    @PostMapping(path = "/{roomId}/file", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ApiResponseMessageAndData<String> uploadFile(Authentication auth,
            @PathVariable UUID roomId, @Valid @ModelAttribute UploadFileMessage uploadFileMessage) {
        log.info("Chat Controller : uploadFile");

        log.info("Chat Controller : uploadFile - Parse JWT Authentication");
        JwtAuthentication jwt = (JwtAuthentication) auth;
        UUID userId = UUID.fromString(jwt.claim("id"));

        String fileUrl = chatService.uploadFileMessage(roomId, userId, uploadFileMessage);

        ApiResponseMessageAndData<String> response = new ApiResponseMessageAndData<>();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setMessage("File uploaded successfully");
        response.setData(fileUrl);

        return response;
    }

}
