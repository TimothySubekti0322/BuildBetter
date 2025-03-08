package com.buildbetter.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.buildbetter.shared.dto.ApiResponseMessageAndData;
import com.buildbetter.shared.dto.ApiResponseMessageOnly;
import com.buildbetter.shared.dto.ApiResponseWithData;
import com.buildbetter.user.dto.ForgotPasswordRequest;
import com.buildbetter.user.dto.LoginRequest;
import com.buildbetter.user.dto.LoginResponse;
import com.buildbetter.user.dto.RegisterUserRequest;
import com.buildbetter.user.dto.ResetPasswordRequest;
import com.buildbetter.user.dto.SendOTPRequest;
import com.buildbetter.user.dto.VerifiedUserRequest;
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

    @PostMapping("/register")
    public ApiResponseMessageAndData<String> registerUser(@Valid @RequestBody RegisterUserRequest request) {
        log.info("Auth Controller : register user : (payload) " + request);
        String message = authService.registerUser(request);

        ApiResponseMessageAndData<String> response = new ApiResponseMessageAndData<>();
        response.setCode(HttpStatus.CREATED.value());
        response.setStatus(HttpStatus.CREATED.name());
        response.setMessage("Verification code sent to" + request.getEmail());
        response.setData(message);

        return response;
    }

    @PostMapping("/login")
    public ApiResponseWithData<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Auth Controller : login user : (payload) " + request);
        LoginResponse loginResponse = authService.login(request);

        ApiResponseWithData<LoginResponse> response = new ApiResponseWithData<>();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setData(loginResponse);

        return response;
    }

    @PostMapping("/send-otp")
    public ApiResponseMessageOnly sendOtp(@Valid @RequestBody SendOTPRequest request) {
        log.info("Auth Controller : send otp : (payload) " + request);

        otpService.sendOtp(request);

        ApiResponseMessageOnly response = new ApiResponseMessageOnly();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setMessage("OTP sent successfully to " + request.getEmail());

        return response;
    }

    @PostMapping("/verify-user")
    public ApiResponseMessageOnly verifyUser(@Valid @RequestBody VerifiedUserRequest request) {
        log.info("Auth Controller : verify user : (payload) " + request);

        String message = authService.verifyUser(request);

        ApiResponseMessageOnly response = new ApiResponseMessageOnly();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setMessage(message);

        return response;
    }

    @PostMapping("/forgot-password")
    public ApiResponseMessageOnly forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("Auth Controller : forgot password : (payload) " + request);

        String message = forgotPassword.forgotPassword(request.getEmail());

        ApiResponseMessageOnly response = new ApiResponseMessageOnly();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setMessage(message);

        return response;
    }

    @PostMapping("/reset-password")
    public ApiResponseMessageOnly postMethodName(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("Auth Controller : reset password : (payload) " + request);

        String message = forgotPassword.resetPassword(request);

        ApiResponseMessageOnly response = new ApiResponseMessageOnly();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setMessage(message);

        return response;
    }

}
