package com.buildbetter.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.buildbetter.auth.dto.LoginRequest;
import com.buildbetter.auth.dto.LoginResponse;
import com.buildbetter.auth.service.AuthService;
import com.buildbetter.shared.dto.ApiResponseWithData;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Slf4j
public class AuthController {

    @Autowired
    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponseWithData<LoginResponse> postMethodName(@RequestBody LoginRequest request) {
        log.info("Controller : " + request);
        LoginResponse loginResponse = authService.login(request);
        return new ApiResponseWithData<>(200, "OK", loginResponse);
    }

}
