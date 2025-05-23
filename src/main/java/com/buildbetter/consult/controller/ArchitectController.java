package com.buildbetter.consult.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.buildbetter.consult.dto.architect.LoginRequest;
import com.buildbetter.consult.dto.architect.LoginResponse;
import com.buildbetter.consult.dto.architect.RegisterArchitectRequest;
import com.buildbetter.consult.dto.architect.UpdateArchitectRequest;
import com.buildbetter.consult.model.Architect;
import com.buildbetter.consult.service.ArchitectService;
import com.buildbetter.shared.dto.ApiResponseMessageOnly;
import com.buildbetter.shared.dto.ApiResponseWithData;
import com.buildbetter.shared.security.JwtAuthentication;
import com.buildbetter.shared.security.annotation.IsAdmin;
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

    @PatchMapping("")
    @IsArchitect
    public ApiResponseMessageOnly updateArchitect(Authentication auth,
            @Valid @RequestBody UpdateArchitectRequest request) {
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

    @GetMapping("")
    @IsAdmin
    public ApiResponseWithData<List<Architect>> getAllArchitects() {
        log.info("Architect Controller : getAllArchitects");

        List<Architect> architects = architectService.getAllArchitects();

        ApiResponseWithData<List<Architect>> response = new ApiResponseWithData<>();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setData(architects);

        return response;
    }

    @GetMapping("/me")
    @IsArchitect
    public ApiResponseWithData<Architect> getCurrentArchitect(Authentication auth) {
        log.info("Architect Controller : getCurrentArchitect");

        log.info("Architect Controller : getCurrentArchitect - Parse JWT Authentication");
        JwtAuthentication jwt = (JwtAuthentication) auth;
        UUID architectId = UUID.fromString(jwt.claim("id"));

        Architect architect = architectService.getArchitectById(architectId);

        ApiResponseWithData<Architect> response = new ApiResponseWithData<>();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setData(architect);

        return response;
    }

    @GetMapping("/{id}")
    @IsAdmin
    public ApiResponseWithData<Architect> getArchitectById(@PathVariable UUID id) {
        log.info("Architect Controller : getArchitectById");

        Architect architect = architectService.getArchitectById(id);

        ApiResponseWithData<Architect> response = new ApiResponseWithData<>();
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

}
