package com.buildbetter.consultation.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.buildbetter.consultation.model.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findBySender(String sender);

    Optional<Payment> findByPaymentMethod(String paymentMethod);

    Optional<Payment> findByConsultationId(UUID id);
}
