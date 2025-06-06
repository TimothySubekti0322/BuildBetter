package com.buildbetter.consultation.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.buildbetter.consultation.constant.CancellationReason;
import com.buildbetter.consultation.constant.ConsultationStatus;
import com.buildbetter.consultation.dto.payment.GetPaymentConsultationResponse;
import com.buildbetter.consultation.dto.payment.UploadPaymentConsultationRequest;
import com.buildbetter.consultation.model.Consultation;
import com.buildbetter.consultation.model.Payment;
import com.buildbetter.consultation.repository.ConsultationRepository;
import com.buildbetter.consultation.repository.PaymentRepository;
import com.buildbetter.consultation.websocket.confirmation.service.ConfirmationService;
import com.buildbetter.shared.constant.S3Folder;
import com.buildbetter.shared.exception.BadRequestException;
import com.buildbetter.shared.exception.ForbiddenException;
import com.buildbetter.shared.util.S3Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

        private final ConsultationService consultationService;
        private final ConfirmationService confirmationService;
        private final ConsultationRepository consultationRepository;
        private final PaymentRepository paymentRepository;
        private final S3Service s3Service;

        public UUID UploadPaymentProof(UUID consultationId, UploadPaymentConsultationRequest request) {
                log.info("Consultation Service : payConsult");

                Consultation consultation = consultationRepository.findById(consultationId)
                                .orElseThrow(() -> new BadRequestException("Consultation not found"));

                // Optional<Payment> existingPayment =
                // paymentRepository.findByConsultationIdExplicit(consultationId);

                Payment existingPayment = consultation.getPayment();

                if (existingPayment == null) {
                        existingPayment = new Payment();
                        existingPayment.setConsultation(consultation);
                        existingPayment.setPaymentMethod("");
                        existingPayment.setProofPayment("");
                        existingPayment.setSender("");
                        existingPayment.setUploadProofPayment(0);
                }

                Integer numberOfUploadProofOfPayment = existingPayment != null
                                ? existingPayment.getUploadProofPayment()
                                : 0;

                log.info("ConsultationService : Check Consultation status");
                Boolean statusIsWaitingForPayment = ConsultationStatus.WAITING_FOR_PAYMENT.getStatus()
                                .equalsIgnoreCase(consultation.getStatus());

                // Boolean statusIsCancelledDueToInvalidPayment =
                // ConsultationStatus.CANCELLED.getStatus()
                // .equals(consultation.getStatus())
                // && CancellationReason.INVALID_PAYMENT.getReason()
                // .equalsIgnoreCase(consultation.getReason());

                // If Status is not waiting for payment or cancelled due to invalid payment
                // if (!statusIsWaitingForPayment && !statusIsCancelledDueToInvalidPayment) {
                // throw new BadRequestException("Consultation is not in a valid state for
                // payment");
                // }

                if (!statusIsWaitingForPayment) {
                        throw new BadRequestException("Consultation is not in waiting for payment state");
                }

                if (numberOfUploadProofOfPayment >= 2) {
                        throw new ForbiddenException("Payment Attempt limit reached");
                }

                Boolean isExpired = LocalDateTime.now().isAfter(consultation.getCreatedAt()
                                .plusMinutes(10));

                log.info("ConsultationService : Check if consultation payment is expired");
                if (isExpired) {
                        // Update Payment Attempt
                        existingPayment.setUploadProofPayment(numberOfUploadProofOfPayment + 1);
                        paymentRepository.save(existingPayment);

                        // Update Consultation status to cancelled
                        consultation.setStatus(ConsultationStatus.CANCELLED.getStatus());
                        consultation.setReason(CancellationReason.INVALID_PAYMENT.getReason());
                        consultation.setCreatedAt(LocalDateTime.now());
                        consultationRepository.save(consultation);

                        log.info("Consultation Service : consultation CreatedAt is set to now : {}",
                                        consultation.getCreatedAt());

                        throw new BadRequestException("Consultation payment is expired");
                }

                log.info("Consultation Service : Upload proof of payment to S3");
                String proofUrl = s3Service.uploadFile(request.getImage(), S3Folder.PROOF_OF_PAYMENTS,
                                consultationId.toString());

                if (existingPayment != null && !existingPayment.getProofPayment().isBlank()) {
                        log.info("Payment Service : UploadPaymentProof - Delete existing proof of payment");
                        s3Service.deleteFile(existingPayment.getProofPayment());
                }

                existingPayment.setUploadProofPayment(existingPayment.getUploadProofPayment() + 1);

                existingPayment.setProofPayment(proofUrl);
                existingPayment.setPaymentMethod(request.getPaymentMethod());
                existingPayment.setSender(request.getSender());

                paymentRepository.save(existingPayment);

                consultation.setStatus(ConsultationStatus.WAITING_FOR_CONFIRMATION.getStatus());
                consultationRepository.save(consultation);

                return existingPayment.getId();
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

        public GetPaymentConsultationResponse getConsultationPayment(UUID consultationId) {
                log.info("Payment Service : getConsultationPayment");

                Consultation consultation = consultationRepository.findById(consultationId)
                                .orElseThrow(() -> new BadRequestException("Consultation not found"));

                Payment payment = consultation.getPayment();

                if (payment == null) {
                        throw new BadRequestException("Payment not found for consultation");
                }

                GetPaymentConsultationResponse response = new GetPaymentConsultationResponse();
                // Set payment details
                response.setPaymentId(payment.getId());
                response.setProofPayment(payment.getProofPayment());
                response.setUploadProofPayment(payment.getUploadProofPayment());
                response.setPaymentMethod(payment.getPaymentMethod());
                response.setSender(payment.getSender());

                // Set consultation details
                response.setConsultationId(consultation.getId());
                response.setUserId(consultation.getUserId());
                response.setArchitectId(consultation.getArchitectId());
                response.setRoomId(consultation.getRoomId());
                response.setType(consultation.getType());
                response.setTotal(consultation.getTotal());
                response.setStatus(consultation.getStatus());
                response.setReason(consultation.getReason());
                response.setLocation(consultation.getLocation());
                response.setLocationDescription(consultation.getLocationDescription());
                response.setStartDate(consultation.getStartDate());
                response.setEndDate(consultation.getEndDate());
                response.setCreatedAt(consultation.getCreatedAt());

                return response;
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

        public Integer markPaymentAsExpired(UUID consultationId) {
                log.info("Payment Service : markPaymentAsExpired");
                Consultation consultation = consultationRepository.findById(consultationId)
                                .orElseThrow(() -> new BadRequestException("Consultation not found"));

                Integer attempt = consultation.getPayment() != null
                                ? consultation.getPayment().getUploadProofPayment()
                                : 0;

                Boolean isWaitingForPayment = ConsultationStatus.WAITING_FOR_PAYMENT.getStatus()
                                .equalsIgnoreCase(consultation.getStatus());

                Boolean isCancelledDueToInvalidPayment = ConsultationStatus.CANCELLED.getStatus()
                                .equalsIgnoreCase(consultation.getStatus())
                                && CancellationReason.INVALID_PAYMENT.getReason()
                                                .equalsIgnoreCase(consultation.getReason());

                if (Boolean.FALSE.equals(isWaitingForPayment) &&
                                Boolean.FALSE.equals(isCancelledDueToInvalidPayment)) {
                        throw new BadRequestException(
                                        "Consultation is not in a valid state to mark payment as expired");
                }

                if (attempt >= 2) {
                        throw new ForbiddenException("Payment Attempt limit reached");
                }

                Integer newAttempt = attempt + 1;

                Payment existingPayment = consultation.getPayment();

                if (existingPayment == null) {
                        existingPayment = Payment.builder()
                                        .proofPayment("")
                                        .uploadProofPayment(newAttempt)
                                        .sender("")
                                        .paymentMethod("")
                                        .build();

                        existingPayment.setConsultation(consultation);
                }

                existingPayment.setUploadProofPayment(newAttempt);

                paymentRepository.save(existingPayment);

                consultation.setStatus(ConsultationStatus.CANCELLED.getStatus());
                consultation.setReason(CancellationReason.INVALID_PAYMENT.getReason());
                consultationRepository.save(consultation);

                return newAttempt;
        }

        public void repayPayment(UUID consultationId) {
                log.info("Payment Service : repayPayment");
                Consultation consultation = consultationRepository.findById(consultationId)
                                .orElseThrow(() -> new BadRequestException("Consultation not found"));

                Payment payment = consultation.getPayment();

                log.info("Payment Service : Check Consultation status");

                Boolean statusIsWaitingForPayment = ConsultationStatus.WAITING_FOR_PAYMENT.getStatus()
                                .equalsIgnoreCase(consultation.getStatus());
                Boolean statusIsCancelledDueToInvalidPayment = ConsultationStatus.CANCELLED.getStatus()
                                .equalsIgnoreCase(consultation.getStatus())
                                && CancellationReason.INVALID_PAYMENT.getReason()
                                                .equalsIgnoreCase(consultation.getReason());

                if (Boolean.FALSE.equals(statusIsWaitingForPayment) &&
                                Boolean.FALSE.equals(statusIsCancelledDueToInvalidPayment)) {
                        throw new BadRequestException(
                                        "Consultation is not in a valid state to repay payment");
                }

                log.info("Payment Service : Check Payment status");
                if (payment != null && payment.getUploadProofPayment() >= 2) {
                        throw new ForbiddenException("Payment Attempt limit reached");
                }

                consultation.setStatus(ConsultationStatus.WAITING_FOR_PAYMENT.getStatus());
                consultation.setReason(null);
                consultation.setCreatedAt(LocalDateTime.now());
                consultationRepository.save(consultation);

                log.info("Payment Service : repayPayment - Consultation status set to WAITING_FOR_PAYMENT, reason to null, and createdAt to now");
        }
}
