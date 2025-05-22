package com.buildbetter.consult.dto.consult;

import java.time.LocalDateTime;

import lombok.Value;

@Value
public class ConsultDateRange {
    LocalDateTime startDate;
    LocalDateTime endDate;
}
