package com.buildbetter.consultation.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "consultation_id", nullable = false)
    private UUID consultationId;

    @Column(name = "proof_payment", nullable = false)
    private String proofPayment;

    @Column(name = "upload_proof_payment") // Count number of times the proof of payment has been uploaded
    private Integer uploadProofPayment;

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;

    @Column(name = "sender", nullable = false)
    private String sender;
}
