package com.buildbetter.consultation.dto.consultation;

import java.time.LocalDateTime;

import com.buildbetter.consultation.validation.annotation.ValidConsultType;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateConsultationRequest {
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @ValidConsultType
    private String type;

    @Min(value = 1, message = "Field 'total' must be greater than 0")
    private Integer total;

    private String location;
}
