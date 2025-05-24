package com.buildbetter.consultation.dto.consultation;

import java.time.LocalDateTime;

import lombok.Value;

@Value
public class ConsultationDateRange {
    LocalDateTime startDate;
    LocalDateTime endDate;
}
