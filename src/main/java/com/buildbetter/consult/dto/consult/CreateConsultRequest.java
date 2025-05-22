package com.buildbetter.consult.dto.consult;

import java.time.LocalDateTime;
import java.util.UUID;

import com.buildbetter.consult.validation.annotation.ValidConsultType;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateConsultRequest {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private UUID architectId;

    @ValidConsultType
    private String type;

    @Min(value = 1, message = "Field 'total' must be greater than 0")
    private Integer total;
}
