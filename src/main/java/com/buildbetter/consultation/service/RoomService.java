package com.buildbetter.consultation.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.buildbetter.consultation.dto.room.CreateRoomRequest;
import com.buildbetter.consultation.model.Room;
import com.buildbetter.consultation.repository.RoomRepository;
import com.buildbetter.shared.exception.BadRequestException;
import com.buildbetter.shared.exception.ForbiddenException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomService {

    private final RoomRepository roomRepository;

    public UUID createRoom(CreateRoomRequest request) {
        log.info("Room Service : createRoomRepository");

        log.info("Room Service : createRoomRepository - Check wheter room is exist for user");

        Optional<Room> existingRoom = roomRepository.findByUserIdAndArchitectId(request.getUserId(),
                request.getArchitectId());

        // Room Already Exist
        if (existingRoom.isPresent()) {
            log.info("Room Service : createRoomRepository - Room Already Exist for user, updating existing room");
            existingRoom.get().setStartTime(request.getStartTime());
            existingRoom.get().setEndTime(request.getEndTime());

            return roomRepository.save(existingRoom.get()).getId();
        }

        // Room Not Exist
        log.info("Room Service : createRoomRepository - Room Not Exist for user, creating new room");

        Room newRoom = Room.builder()
                .architectId(request.getArchitectId())
                .userId(request.getUserId())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();

        UUID roomId = roomRepository.save(newRoom).getId();

        return roomId;

    }

    public List<Room> getAllRooms() {
        log.info("Room Service : getAllRooms");
        return roomRepository.findAll();
    }

    public Room getRoomById(UUID roomId, UUID userId) {
        log.info("Room Service : getRoomById - Room ID: {}", roomId);

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BadRequestException("Room not found with ID: " + roomId));

        if (!room.getUserId().equals(userId) && !room.getArchitectId().equals(userId)) {
            log.error("Room Service : getRoomById - User ID: {} is not authorized to access this room", userId);
            throw new ForbiddenException("User is not authorized to access this room");
        }

        return room;
    }

}
