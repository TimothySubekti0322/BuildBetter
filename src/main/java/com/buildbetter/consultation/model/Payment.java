package com.buildbetter.consultation.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

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

    @Column(name = "proof_payment", nullable = false)
    private String proofPayment;

    @Column(name = "upload_proof_payment") // Count number of times the proof of payment has been uploaded
    private Integer uploadProofPayment;

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;

    @Column(name = "sender", nullable = false)
    private String sender;

    // One-to-One relationship with Consultation
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id", nullable = false, unique = true)
    @ToString.Exclude // Prevent circular reference in toString
    @EqualsAndHashCode.Exclude // Prevent circular reference in equals/hashCode
    private Consultation consultation;

    // Helper method to get consultation ID
    public UUID getConsultationId() {
        return consultation != null ? consultation.getId() : null;
    }
}
