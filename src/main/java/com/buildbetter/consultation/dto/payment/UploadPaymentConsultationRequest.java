package com.buildbetter.consultation.dto.payment;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadPaymentConsultationRequest {
    @NotNull(message = "Field 'image' is required")
    MultipartFile image;

    @NotBlank(message = "Field 'paymentMethod' is required")
    String paymentMethod;

    @NotBlank(message = "Field 'sender' is required")
    String sender;
}
