package com.buildbetter.consultation.dto.room;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoomRequest {
    @NotBlank(message = "Field 'architectId' is required")
    private UUID architectId;

    @NotBlank(message = "Field 'userId' is required")
    private UUID userId;

    @NotBlank(message = "Field 'startTime' is required")
    private LocalDateTime startTime;

    @NotBlank(message = "Field 'endTime' is required")
    private LocalDateTime endTime;
}
