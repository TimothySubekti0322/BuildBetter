package com.buildbetter.consultation.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.buildbetter.consultation.dto.architect.ArchitectResponse;
import com.buildbetter.consultation.dto.architect.ChangeArchitectPasswordRequest;
import com.buildbetter.consultation.dto.architect.LoginRequest;
import com.buildbetter.consultation.dto.architect.LoginResponse;
import com.buildbetter.consultation.dto.architect.RegisterArchitectRequest;
import com.buildbetter.consultation.dto.architect.UpdateArchitectRequest;
import com.buildbetter.consultation.dto.consultation.Schedule;
import com.buildbetter.consultation.model.Consultation;
import com.buildbetter.consultation.service.ArchitectService;
import com.buildbetter.consultation.service.ConsultationService;
import com.buildbetter.shared.dto.ApiResponseMessageOnly;
import com.buildbetter.shared.dto.ApiResponseWithData;
import com.buildbetter.shared.security.JwtAuthentication;
import com.buildbetter.shared.security.annotation.IsAdmin;
import com.buildbetter.shared.security.annotation.IsAdminOrArchitect;
import com.buildbetter.shared.security.annotation.IsAdminOrUser;
import com.buildbetter.shared.security.annotation.IsArchitect;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/architects")
@Slf4j
public class ArchitectController {

    private final ArchitectService architectService;
    private final ConsultationService consultationService;

    @PostMapping("")
    @IsAdmin
    public ApiResponseMessageOnly registerArchitect(@Valid @RequestBody RegisterArchitectRequest request) {
        log.info("Architect Controller : registerArchitect");

        architectService.registerArchitect(request);

        ApiResponseMessageOnly response = new ApiResponseMessageOnly();
        response.setCode(201);
        response.setStatus(HttpStatus.CREATED.name());
        response.setMessage("Architect registered successfully");
        return response;
    }

    @PostMapping("/login")
    public ApiResponseWithData<LoginResponse> loginArchitect(@Valid @RequestBody LoginRequest request) {
        log.info("Architect Controller : loginArchitect");

        LoginResponse response = architectService.loginArchitect(request);

        ApiResponseWithData<LoginResponse> apiResponse = new ApiResponseWithData<>();
        apiResponse.setCode(HttpStatus.OK.value());
        apiResponse.setStatus(HttpStatus.OK.name());
        apiResponse.setData(response);

        return apiResponse;
    }

    @PatchMapping(path = "", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @IsArchitect
    public ApiResponseMessageOnly updateArchitect(Authentication auth,
            @Valid @ModelAttribute UpdateArchitectRequest request) {
        log.info("Architect Controller : updateArchitect");

        log.info("Architect Controller : updateArchitect - Parse JWT Authentication");
        JwtAuthentication jwt = (JwtAuthentication) auth;
        UUID userId = UUID.fromString(jwt.claim("id"));

        architectService.updateArchitect(userId, request);

        ApiResponseMessageOnly response = new ApiResponseMessageOnly();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setMessage("Architect updated successfully");
        return response;
    }

    @PatchMapping("/change-password")
    @IsArchitect
    public ApiResponseMessageOnly changeArchitectPassword(Authentication auth,
            @RequestBody @Valid ChangeArchitectPasswordRequest request) {
        log.info("Architect Controller : changeArchitectPassword");

        log.info("Architect Controller : changeArchitectPassword - Parse JWT Authentication");
        JwtAuthentication jwt = (JwtAuthentication) auth;
        UUID architectId = UUID.fromString(jwt.claim("id"));

        architectService.changePassword(architectId, request);

        ApiResponseMessageOnly response = new ApiResponseMessageOnly();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setMessage("Architect password changed successfully");
        return response;
    }

    @GetMapping("")
    @IsAdminOrUser
    public ApiResponseWithData<List<ArchitectResponse>> getAllArchitects(
            Authentication auth,
            @RequestParam(value = "notContacted", required = false, defaultValue = "false") boolean notContacted,
            @RequestParam(value = "city", required = false) String city) {
        log.info("Architect Controller : getAllArchitects");

        log.info("Architect Controller : getAllArchitects - Parse JWT Authentication");
        JwtAuthentication jwt = (JwtAuthentication) auth;
        UUID userId = UUID.fromString(jwt.claim("id"));

        List<ArchitectResponse> architects = architectService.getAllArchitects(userId, notContacted, city);

        ApiResponseWithData<List<ArchitectResponse>> response = new ApiResponseWithData<>();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setData(architects);

        return response;
    }

    @GetMapping("/me")
    @IsArchitect
    public ApiResponseWithData<ArchitectResponse> getCurrentArchitect(Authentication auth) {
        log.info("Architect Controller : getCurrentArchitect");

        log.info("Architect Controller : getCurrentArchitect - Parse JWT Authentication");
        JwtAuthentication jwt = (JwtAuthentication) auth;
        UUID architectId = UUID.fromString(jwt.claim("id"));

        ArchitectResponse architect = architectService.getArchitectById(architectId);

        ApiResponseWithData<ArchitectResponse> response = new ApiResponseWithData<>();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setData(architect);

        return response;
    }

    @GetMapping("/{id}")
    @IsAdmin
    public ApiResponseWithData<ArchitectResponse> getArchitectById(@PathVariable UUID id) {
        log.info("Architect Controller : getArchitectById");

        ArchitectResponse architect = architectService.getArchitectById(id);

        ApiResponseWithData<ArchitectResponse> response = new ApiResponseWithData<>();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setData(architect);

        return response;
    }

    @DeleteMapping("/{id}")
    @IsAdmin
    public ApiResponseMessageOnly deleteArchitect(@PathVariable UUID id) {
        log.info("Architect Controller : deleteArchitect");

        architectService.deleteArchitect(id);

        ApiResponseMessageOnly response = new ApiResponseMessageOnly();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setMessage("Architect deleted successfully");

        return response;
    }

    @GetMapping("/{id}/consultations")
    @IsAdminOrArchitect
    public ApiResponseWithData<List<Consultation>> getAllArchitectConsults(@PathVariable UUID id,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "includeCancelled", required = false) Boolean includeCancelled,
            @RequestParam(value = "upcoming", required = false) Boolean upcoming) {
        log.info("Architect Controller : getAllArchitectConsults");

        List<Consultation> consults = consultationService.getAllConsultsByArchitectId(id, type, status,
                includeCancelled,
                upcoming);

        ApiResponseWithData<List<Consultation>> response = new ApiResponseWithData<>();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setData(consults);
        return response;
    }

    @GetMapping("/{id}/schedules")
    public ApiResponseWithData<List<Schedule>> getArchitectSchedules(@PathVariable UUID id) {
        log.info("Consult Controller : getArchitectSchedules");

        List<Schedule> schedules = consultationService.getArchitectSchedules(id);

        ApiResponseWithData<List<Schedule>> response = new ApiResponseWithData<>();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setData(schedules);

        return response;
    }

}
