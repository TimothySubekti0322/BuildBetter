package com.buildbetter.consultation.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.buildbetter.consultation.dto.consultation.CreateConsultationRequest;
import com.buildbetter.consultation.model.Consultation;
import com.buildbetter.consultation.model.Payment;
import com.buildbetter.consultation.service.ConsultationService;
import com.buildbetter.consultation.service.PaymentService;
import com.buildbetter.shared.dto.ApiResponseMessageAndData;
import com.buildbetter.shared.dto.ApiResponseWithData;
import com.buildbetter.shared.security.JwtAuthentication;
import com.buildbetter.shared.security.annotation.Authenticated;
import com.buildbetter.shared.security.annotation.IsAdmin;
import com.buildbetter.shared.security.annotation.IsAdminOrUser;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Slf4j
public class ConsultationController {

    private final ConsultationService consultationService;
    private final PaymentService paymentService;

    @PostMapping("/consultations")
    @Authenticated
    public ApiResponseMessageAndData<UUID> createConsults(Authentication auth,
            @Valid @RequestBody CreateConsultationRequest request) {

        log.info("Consult Controller : createConsults");

        log.info("Consult Controller : createConsults - Parse JWT Authentication");
        JwtAuthentication jwt = (JwtAuthentication) auth;
        UUID userId = UUID.fromString(jwt.claim("id"));

        UUID consultId = consultationService.createConsult(request, userId);

        ApiResponseMessageAndData<UUID> response = new ApiResponseMessageAndData<>();
        response.setCode(HttpStatus.CREATED.value());
        response.setStatus(HttpStatus.CREATED.name());
        response.setMessage("Consult created successfully");
        response.setData(consultId);

        return response;
    }

    // @GetMapping("/consultations/{id}/schedules")
    // public ApiResponseWithData<List<Schedule>>
    // getArchitectSchedules(@PathVariable UUID id) {
    // log.info("Consult Controller : getArchitectSchedules");

    // List<Schedule> schedules = consultationService.getArchitectSchedules(id);

    // ApiResponseWithData<List<Schedule>> response = new ApiResponseWithData<>();
    // response.setCode(HttpStatus.OK.value());
    // response.setStatus(HttpStatus.OK.name());
    // response.setData(schedules);

    // return response;
    // }

    @GetMapping("/consultations")
    @IsAdmin
    public ApiResponseWithData<List<Consultation>> getAllConsultations(
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "includeCancelled", required = false) Boolean includeCancelled,
            @RequestParam(value = "upcoming", required = false) Boolean upcoming) {
        log.info("Consult Controller : getAllConsults");

        List<Consultation> consults = consultationService.getAllConsults(type, status, includeCancelled, upcoming);

        ApiResponseWithData<List<Consultation>> response = new ApiResponseWithData<>();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setData(consults);
        return response;
    }

    @GetMapping("/consultations/{id}")
    public ApiResponseWithData<Consultation> getConsultationById(@PathVariable UUID id) {
        log.info("Consult Controller : getConsultById");

        Consultation consult = consultationService.getConsultById(id);

        ApiResponseWithData<Consultation> response = new ApiResponseWithData<>();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setData(consult);

        return response;
    }

    @GetMapping("/consultations/{consultationId}/payments")
    public ApiResponseMessageAndData<Payment> getConsultationPayment(@PathVariable UUID consultationId) {
        log.info("Consult Controller : getConsultationPayment");

        Payment payment = paymentService.getConsultationPayment(consultationId);

        ApiResponseMessageAndData<Payment> response = new ApiResponseMessageAndData<>();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setMessage("Payment fetched successfully");
        response.setData(payment);

        return response;
    }

    @GetMapping("/users/consultations")
    @IsAdminOrUser
    public ApiResponseWithData<List<Consultation>> getUserConsults(
            Authentication auth,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "includeCancelled", required = false) Boolean includeCancelled,
            @RequestParam(value = "upcoming", required = false) Boolean upcoming) {
        log.info("Consult Controller : getUserConsults");

        log.info("Consult Controller : getUserConsults - Parse JWT Authentication");
        JwtAuthentication jwt = (JwtAuthentication) auth;
        UUID userId = UUID.fromString(jwt.claim("id"));

        List<Consultation> consults = consultationService.getUserConsultations(userId, type, status, includeCancelled,
                upcoming);

        ApiResponseWithData<List<Consultation>> response = new ApiResponseWithData<>();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setData(consults);

        return response;
    }
}
