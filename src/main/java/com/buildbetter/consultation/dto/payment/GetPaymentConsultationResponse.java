package com.buildbetter.consultation.dto.payment;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetPaymentConsultationResponse {

    // Payment fields
    private UUID paymentId;
    private String proofPayment;
    private Integer uploadProofPayment;
    private String paymentMethod;
    private String sender;

    // Consultation fields
    private UUID consultationId;
    private UUID userId;
    private UUID architectId;
    private UUID roomId;
    private String type;
    private Integer total;
    private String status;
    private String reason;
    private String location;
    private String locationDescription;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;

}
