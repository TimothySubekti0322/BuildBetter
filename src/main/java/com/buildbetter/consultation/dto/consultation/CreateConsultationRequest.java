package com.buildbetter.consultation.dto.consultation;

import java.time.LocalDateTime;
import java.util.UUID;

import com.buildbetter.consultation.validation.annotation.ValidConsultType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateConsultationRequest {

    @NotNull(message = "Field 'startDate' is required")
    private LocalDateTime startDate;
    @NotNull(message = "Field 'endDate' is required")
    private LocalDateTime endDate;
    @NotNull(message = "Field 'architectId' is required")
    private UUID architectId;

    @NotNull(message = "Field 'type' is required")
    @ValidConsultType
    private String type;

    @NotNull(message = "Field 'total' is required")
    @Min(value = 1, message = "Field 'total' must be greater than 0")
    private Integer total;

    private String location;

    private String locationDescription;
}
