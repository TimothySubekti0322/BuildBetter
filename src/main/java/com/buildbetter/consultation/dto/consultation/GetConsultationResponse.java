package com.buildbetter.consultation.dto.consultation;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetConsultationResponse {
    private UUID id;
    private UUID userId;
    private String userName;
    private String userCity;
    private UUID architectId;
    private String architectName;
    private String architectCity;
    private UUID roomId;
    private String type;
    private Integer total;
    private String status;
    private String reason;
    private String location;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;
}
