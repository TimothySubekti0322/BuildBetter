package com.buildbetter.consult.controller;

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

import com.buildbetter.consult.dto.consult.CreateConsultRequest;
import com.buildbetter.consult.dto.consult.Schedule;
import com.buildbetter.consult.model.Consult;
import com.buildbetter.consult.service.ConsultService;
import com.buildbetter.shared.dto.ApiResponseMessageAndData;
import com.buildbetter.shared.dto.ApiResponseWithData;
import com.buildbetter.shared.security.JwtAuthentication;
import com.buildbetter.shared.security.annotation.Authenticated;
import com.buildbetter.shared.security.annotation.IsAdmin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/consults")
@Slf4j
public class ConsultController {
    private final ConsultService consultService;

    @PostMapping("")
    @Authenticated
    public ApiResponseMessageAndData<UUID> createConsults(Authentication auth,
            @Valid @RequestBody CreateConsultRequest request) {

        log.info("Consult Controller : createConsults");

        log.info("Consult Controller : createConsults - Parse JWT Authentication");
        JwtAuthentication jwt = (JwtAuthentication) auth;
        UUID userId = UUID.fromString(jwt.claim("id"));

        UUID consultId = consultService.createConsult(request, userId);

        ApiResponseMessageAndData<UUID> response = new ApiResponseMessageAndData<>();
        response.setCode(HttpStatus.CREATED.value());
        response.setStatus(HttpStatus.CREATED.name());
        response.setMessage("Consult created successfully");
        response.setData(consultId);

        return response;
    }

    @GetMapping("/{id}/schedules")
    public ApiResponseWithData<List<Schedule>> getArchitectSchedules(@PathVariable UUID id) {
        log.info("Consult Controller : getArchitectSchedules");

        List<Schedule> schedules = consultService.getArchitectSchedules(id);

        ApiResponseWithData<List<Schedule>> response = new ApiResponseWithData<>();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setData(schedules);

        return response;
    }

    @GetMapping("/{id}")
    @IsAdmin
    public ApiResponseWithData<List<Consult>> getAllConsultsByArchitectId(@PathVariable UUID id,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "includeCancelled", required = false) Boolean includeCancelled) {
        log.info("Consult Controller : getAllConsultsByArchitectId");

        List<Consult> consults = consultService.getAllConsultsByArchitectId(id, type, status, includeCancelled);

        ApiResponseWithData<List<Consult>> response = new ApiResponseWithData<>();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setData(consults);
        return response;
    }

    @GetMapping("")
    public ApiResponseWithData<List<Consult>> getAllConsults(
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "includeCancelled", required = false) Boolean includeCancelled) {
        log.info("Consult Controller : getAllConsults");

        List<Consult> consults = consultService.getAllConsults(type, status, includeCancelled);

        ApiResponseWithData<List<Consult>> response = new ApiResponseWithData<>();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setData(consults);
        return response;
    }

}
