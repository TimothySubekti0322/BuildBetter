package com.buildbetter.consultation.dto.consultation;

import com.buildbetter.consultation.validation.annotation.ValidCancellationReason;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RejectConsultationRequest {
    @NotBlank(message = "Field 'message' is required")
    @ValidCancellationReason
    private String message;
}
