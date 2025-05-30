package com.buildbetter.consultation.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.buildbetter.consultation.websocket.chat.service.ChatSessionManager;
import com.buildbetter.consultation.websocket.chat.service.RoomTimeoutService;
import com.buildbetter.shared.dto.ApiResponseWithData;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chats/monitor")
@Slf4j
public class ChatMonitorController {
    private final ChatSessionManager sessionManager;
    private final RoomTimeoutService roomTimeoutService;

    @GetMapping("")
    public ApiResponseWithData<Map<String, Object>> getStatus() {
        log.info("Chat Monitor Controller: getStatus");

        Map<String, Object> status = new HashMap<>();
        status.put("totalActiveSessions", sessionManager.getTotalActiveSessions());
        status.put("activeRooms", sessionManager.getActiveRoomIds().size());
        status.put("scheduledTimeouts", roomTimeoutService.getScheduledTimeoutCount());

        ApiResponseWithData<Map<String, Object>> response = new ApiResponseWithData<>();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setData(status);

        return response;
    }

    @GetMapping("/room/{roomId}")
    public ApiResponseWithData<Map<String, Object>> getRoomStatus(@PathVariable UUID roomId) {
        log.info("Chat Monitor Controller: getRoomStatus for room {}", roomId);

        Map<String, Object> roomStatus = new HashMap<>();
        roomStatus.put("roomId", roomId);
        roomStatus.put("activeSessions", sessionManager.getSessionCount(roomId));
        roomStatus.put("hasScheduledTimeout", roomTimeoutService.hasScheduledTimeout(roomId));

        ApiResponseWithData<Map<String, Object>> response = new ApiResponseWithData<>();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setData(roomStatus);

        return response;
    }

    @PostMapping("/room/{roomId}/terminate")
    public ApiResponseWithData<Map<String, String>> terminateRoom(@PathVariable UUID roomId) {
        log.info("Chat Monitor Controller: terminateRoom for room {}", roomId);

        boolean cancelled = roomTimeoutService.cancelRoomTimeout(roomId);

        Map<String, String> result = new HashMap<>();
        result.put("roomId", roomId.toString());
        result.put("status", cancelled ? "terminated" : "no_active_timeout");

        ApiResponseWithData<Map<String, String>> response = new ApiResponseWithData<>();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setData(result);

        return response;
    }

}
