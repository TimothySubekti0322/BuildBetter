package com.buildbetter.consult.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.buildbetter.consult.model.Room;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {
    // Custom query methods can be defined here if needed
    // For example, find by room name or other attributes
    Optional<Room> findByUserIdAndArchitectId(UUID userId, UUID architectId);
}
