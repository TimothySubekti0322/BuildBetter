package com.buildbetter.consultation.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.buildbetter.consultation.model.Room;
import com.buildbetter.consultation.service.RoomService;
import com.buildbetter.shared.dto.ApiResponseWithData;
import com.buildbetter.shared.security.JwtAuthentication;
import com.buildbetter.shared.security.annotation.Authenticated;
import com.buildbetter.shared.security.annotation.IsAdmin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rooms")
@Slf4j
public class RoomController {

    private final RoomService roomService;

    @GetMapping("")
    @IsAdmin
    public ApiResponseWithData<List<Room>> getAllRooms() {
        log.info("Room Controller : getAllRooms");

        List<Room> rooms = roomService.getAllRooms();

        ApiResponseWithData<List<Room>> response = new ApiResponseWithData<>();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setData(rooms);

        return response;
    }

    @GetMapping("/{roomId}")
    @Authenticated
    public ApiResponseWithData<Room> getRoomById(Authentication auth, @PathVariable UUID roomId) {
        log.info("Room Controller : getRoomById");

        log.info("Room Controller : getRoomById - Parse JWT Authentication");
        JwtAuthentication jwt = (JwtAuthentication) auth;
        UUID userId = UUID.fromString(jwt.claim("id"));

        Room room = roomService.getRoomById(roomId, userId);

        ApiResponseWithData<Room> response = new ApiResponseWithData<>();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setData(room);
        return response;
    }

}
