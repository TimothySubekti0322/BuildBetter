package com.buildbetter.user.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.buildbetter.user.model.ResetPasswordToken;

@Repository
public interface ResetPasswordRepository extends JpaRepository<ResetPasswordToken, UUID> {
    Optional<ResetPasswordToken> findByUserIdAndIsUsedFalse(String userId);
}
