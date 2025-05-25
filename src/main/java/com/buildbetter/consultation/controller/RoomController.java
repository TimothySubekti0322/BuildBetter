package com.buildbetter.consultation.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.buildbetter.consultation.model.Room;
import com.buildbetter.consultation.service.RoomService;
import com.buildbetter.shared.dto.ApiResponseWithData;
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

}
