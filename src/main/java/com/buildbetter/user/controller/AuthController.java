package com.buildbetter.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.buildbetter.shared.dto.ApiResponseMessageAndData;
import com.buildbetter.shared.dto.ApiResponseMessageOnly;
import com.buildbetter.shared.dto.ApiResponseWithData;
import com.buildbetter.shared.security.annotation.IsAdmin;
import com.buildbetter.user.dto.auth.ForgotPasswordRequest;
import com.buildbetter.user.dto.auth.LoginRequest;
import com.buildbetter.user.dto.auth.LoginResponse;
import com.buildbetter.user.dto.auth.RegisterUserRequest;
import com.buildbetter.user.dto.auth.ResetPasswordRequest;
import com.buildbetter.user.dto.auth.SendOTPRequest;
import com.buildbetter.user.dto.auth.VerifiedUserRequest;
import com.buildbetter.user.service.AuthService;
import com.buildbetter.user.service.ForgotPassword;
import com.buildbetter.user.service.OtpService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Slf4j
public class AuthController {

    private final OtpService otpService;
    private final AuthService authService;
    private final ForgotPassword forgotPassword;

    @GetMapping("/ping")
    public ApiResponseMessageOnly getHello() {
        ApiResponseMessageOnly response = new ApiResponseMessageOnly();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setMessage("Hello from Auth Controller");
        return response;
    }

    @PostMapping("/register")
    public ApiResponseMessageAndData<String> registerUser(@Valid @RequestBody RegisterUserRequest request) {
        log.info("Auth Controller : registerUser");
        String message = authService.registerUser(request);

        ApiResponseMessageAndData<String> response = new ApiResponseMessageAndData<>();
        response.setCode(HttpStatus.CREATED.value());
        response.setStatus(HttpStatus.CREATED.name());
        response.setMessage("Verification code sent to " + request.getEmail());
        response.setData(message);

        return response;
    }

    @PostMapping("/login")
    public ApiResponseWithData<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Auth Controller : login");
        LoginResponse loginResponse = authService.login(request);

        ApiResponseWithData<LoginResponse> response = new ApiResponseWithData<>();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setData(loginResponse);

        return response;
    }

    @PostMapping("/send-otp")
    public ApiResponseMessageOnly sendOtp(@Valid @RequestBody SendOTPRequest request) {
        log.info("Auth Controller : sendOtp");

        otpService.sendOtp(request);

        ApiResponseMessageOnly response = new ApiResponseMessageOnly();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setMessage("OTP sent successfully to " + request.getEmail());

        return response;
    }

    @PostMapping("/verify-user")
    public ApiResponseMessageOnly verifyUser(@Valid @RequestBody VerifiedUserRequest request) {
        log.info("Auth Controller : verifyUser");

        String message = authService.verifyUser(request);

        ApiResponseMessageOnly response = new ApiResponseMessageOnly();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setMessage(message);

        return response;
    }

    @PostMapping("/forgot-password")
    public ApiResponseMessageOnly forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("Auth Controller : forgotPassword");

        String message = forgotPassword.forgotPassword(request.getEmail());

        ApiResponseMessageOnly response = new ApiResponseMessageOnly();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setMessage(message);

        return response;
    }

    @PostMapping("/reset-password")
    public ApiResponseMessageOnly resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("Auth Controller : resetPassword");

        String message = forgotPassword.resetPassword(request);

        ApiResponseMessageOnly response = new ApiResponseMessageOnly();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setMessage(message);

        return response;
    }

    @PostMapping("/register/architect")
    @IsAdmin
    public ApiResponseMessageAndData<String> registerArchitect(@Valid @RequestBody RegisterUserRequest request) {
        log.info("Auth Controller : registerArchitect");
        String message = authService.registerUser(request);

        ApiResponseMessageAndData<String> response = new ApiResponseMessageAndData<>();
        response.setCode(HttpStatus.CREATED.value());
        response.setStatus(HttpStatus.CREATED.name());
        response.setMessage("Verification code sent to " + request.getEmail());
        response.setData(message);

        return response;
    }

}
