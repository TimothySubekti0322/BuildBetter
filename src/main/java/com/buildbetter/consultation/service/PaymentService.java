package com.buildbetter.consultation.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.buildbetter.consultation.constant.CancellationReason;
import com.buildbetter.consultation.constant.ConsultationStatus;
import com.buildbetter.consultation.dto.payment.UploadPaymentConsultationRequest;
import com.buildbetter.consultation.model.Consultation;
import com.buildbetter.consultation.model.Payment;
import com.buildbetter.consultation.repository.ConsultationRepository;
import com.buildbetter.consultation.repository.PaymentRepository;
import com.buildbetter.shared.constant.S3Folder;
import com.buildbetter.shared.exception.BadRequestException;
import com.buildbetter.shared.util.S3Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final ConsultationRepository consultationRepository;
    private final PaymentRepository paymentRepository;
    private final S3Service s3Service;

    public UUID UploadPaymentProof(UUID consultationId, UploadPaymentConsultationRequest request) {
        log.info("Consultation Service : payConsult");

        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new BadRequestException("Consultation not found"));

        Optional<Payment> existingPayment = paymentRepository.findByConsultationId(consultationId);

        Integer numberOfUploadProofOfPayment = existingPayment.isPresent()
                ? existingPayment.get().getUploadProofPayment()
                : 0;

        log.info("ConsultationService : Check Consultation status");
        Boolean statusIsWaitingForPayment = ConsultationStatus.WAITING_FOR_PAYMENT.getStatus()
                .equalsIgnoreCase(consultation.getStatus());
        Boolean statusIsCancelledDueToInvalidPaymentAndNeverRetried = ConsultationStatus.CANCELLED.getStatus()
                .equals(consultation.getStatus())
                && CancellationReason.INVALID_PAYMENT.getReason().equalsIgnoreCase(consultation.getReason())
                && numberOfUploadProofOfPayment < 2
                && CancellationReason.INVALID_PAYMENT.getReason().equalsIgnoreCase(consultation.getReason());

        if (!statusIsWaitingForPayment && !statusIsCancelledDueToInvalidPaymentAndNeverRetried) {
            throw new BadRequestException("Consultation is not in a valid state for payment");
        }

        log.info("Consultation Service : Upload proof of payment to S3");
        String proofUrl = s3Service.uploadFile(request.getImage(), S3Folder.PROOF_OF_PAYMENTS,
                consultationId.toString());

        if (existingPayment.isPresent() && !existingPayment.get().getProofPayment().isBlank()) {
            log.info("Payment Service : UploadPaymentProof - Delete existing proof of payment");
            s3Service.deleteFile(existingPayment.get().getProofPayment());
        }

        Payment payment = existingPayment.orElseGet(() -> Payment.builder()
                .consultationId(consultationId)
                .uploadProofPayment(0)
                .build());

        payment.setUploadProofPayment(payment.getUploadProofPayment() + 1);

        payment.setProofPayment(proofUrl);
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setSender(request.getSender());

        paymentRepository.save(payment);

        consultation.setStatus(ConsultationStatus.WAITING_FOR_CONFIRMATION.getStatus());
        consultationRepository.save(consultation);

        return payment.getId();
    }

    public List<Payment> getAllPayments() {
        log.info("Payment Service : getAllPayments");
        return paymentRepository.findAll();
    }

    public Payment getPaymentById(UUID id) {
        log.info("Payment Service : getPaymentById");
        return paymentRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Payment not found"));
    }

    public Payment getConsultationPayment(UUID consultationId) {
        log.info("Payment Service : getConsultationPayment");
        return paymentRepository.findByConsultationId(consultationId)
                .orElseThrow(() -> new BadRequestException("Payment not found for consultation"));
    }

    public void deletePayment(UUID id) {
        log.info("Payment Service : deletePayment");
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Payment not found"));

        if (payment.getProofPayment() != null && !payment.getProofPayment().isBlank()) {
            log.info("Payment Service : deletePayment - Delete proof of payment from S3");
            s3Service.deleteFile(payment.getProofPayment());
        }

        Consultation consultation = consultationRepository.findById(payment.getConsultationId())
                .orElseThrow(() -> new BadRequestException("Consultation not found"));

        consultation.setStatus(ConsultationStatus.WAITING_FOR_CONFIRMATION.getStatus());

        consultationRepository.save(consultation);

        paymentRepository.delete(payment);

    }
}
