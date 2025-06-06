package com.buildbetter.consultation.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.buildbetter.consultation.dto.payment.UploadPaymentConsultationRequest;
import com.buildbetter.consultation.model.Payment;
import com.buildbetter.consultation.service.PaymentService;
import com.buildbetter.shared.dto.ApiResponseMessageAndData;
import com.buildbetter.shared.dto.ApiResponseMessageOnly;
import com.buildbetter.shared.security.annotation.Authenticated;
import com.buildbetter.shared.security.annotation.IsAdmin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping(path = "/{consultationId}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ApiResponseMessageAndData<UUID> uploadPaymentProof(@PathVariable UUID consultationId,
            @Valid @ModelAttribute UploadPaymentConsultationRequest request) {

        log.info("Payment Controller : uploadPaymentProof");

        UUID paymentId = paymentService.UploadPaymentProof(consultationId, request);

        ApiResponseMessageAndData<UUID> response = new ApiResponseMessageAndData<>();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setMessage("Payment proof uploaded successfully");
        response.setData(paymentId);
        return response;
    }

    @GetMapping("")
    @IsAdmin
    public ApiResponseMessageAndData<List<Payment>> getAllPayments() {
        log.info("Payment Controller : getAllPayments");

        List<Payment> payment = paymentService.getAllPayments();
        ApiResponseMessageAndData<List<Payment>> response = new ApiResponseMessageAndData<>();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setMessage("Payments fetched successfully");
        response.setData(payment);
        return response;
    }

    @GetMapping("/{id}")
    public ApiResponseMessageAndData<Payment> getPaymentById(@PathVariable UUID id) {
        log.info("Payment Controller : getPaymentById");

        Payment payment = paymentService.getPaymentById(id);
        ApiResponseMessageAndData<Payment> response = new ApiResponseMessageAndData<>();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setMessage("Payment fetched successfully");
        response.setData(payment);
        return response;
    }

    @DeleteMapping("/{id}")
    @IsAdmin
    public ApiResponseMessageOnly deletePayment(@PathVariable UUID id) {
        log.info("Payment Controller : deletePayment");
        paymentService.deletePayment(id);
        ApiResponseMessageOnly response = new ApiResponseMessageOnly();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setMessage("Payment deleted successfully");
        return response;
    }

    @PostMapping("/{consultationId}/expired")
    @Authenticated
    public ApiResponseMessageOnly markPaymentAsExpired(@PathVariable UUID consultationId) {
        log.info("Payment Controller : markPaymentAsExpired");

        Integer attempt = paymentService.markPaymentAsExpired(consultationId);

        ApiResponseMessageOnly response = new ApiResponseMessageOnly();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setMessage("Payment for consultation {" + consultationId
                + "} marked as expired successfully. Attempt: " + attempt);

        return response;
    }

    @PostMapping("/{consultationId}/repay")
    @Authenticated
    public ApiResponseMessageOnly repayPayment(@PathVariable UUID consultationId) {
        log.info("Payment Controller : repayPayment");

        paymentService.repayPayment(consultationId);

        ApiResponseMessageOnly response = new ApiResponseMessageOnly();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setMessage("Payment for consultation {" + consultationId + "} has been successfully repaid.");

        return response;
    }

}