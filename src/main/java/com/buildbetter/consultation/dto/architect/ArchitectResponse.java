package com.buildbetter.consultation.dto.architect;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArchitectResponse {
    private UUID id;
    private String email;
    private String username;
    private String photo;
    private String province;
    private String city;
    private String phoneNumber;
    private Float experience;
    private Integer rateOnline;
    private Integer rateOffline;
    private String portfolio;
    private LocalDateTime createdAt;
}
