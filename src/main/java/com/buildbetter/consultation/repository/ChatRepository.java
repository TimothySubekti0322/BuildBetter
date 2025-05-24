package com.buildbetter.consultation.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.buildbetter.consultation.model.Chat;

@Repository
public interface ChatRepository extends JpaRepository<Chat, UUID> {
    List<Chat> findByRoomIdOrderByCreatedAtAsc(UUID roomId);
}
