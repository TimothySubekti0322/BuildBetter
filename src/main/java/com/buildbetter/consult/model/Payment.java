package com.buildbetter.consult.model;

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

    @Column(name = "consult_id", nullable = false)
    private UUID consultId;

    @Column(name = "proof_payment", nullable = false)
    private String proofPayment;

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;

    @Column(name = "sender", nullable = false)
    private String sender;
}
