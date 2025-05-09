package com.buildbetter.user.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "otps")
public class Otp {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "hashed_otp", nullable = false)
    private String hashedOtp;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_used", nullable = false)
    private boolean isUsed;

    @Column(name = "attempt", nullable = false)
    private int attempt;

    @PrePersist
    public void prePersist() {
        // Generate ID if not present
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.expiredAt == null) {
            this.expiredAt = this.createdAt.plusMinutes(10);
        }
    }

    public void setIsUsed(boolean isUsed) {
        this.isUsed = isUsed;
    }
}
