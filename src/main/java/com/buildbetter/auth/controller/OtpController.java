package com.buildbetter.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.buildbetter.auth.dto.SendOTPRequest;
import com.buildbetter.auth.dto.VerifiedUserRequest;
import com.buildbetter.auth.service.OtpService;
import com.buildbetter.shared.dto.ApiResponseMessageOnly;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/")
@Slf4j
public class OtpController {

    @Autowired
    private final OtpService otpService;

    @PostMapping("/otp/send")
    public ApiResponseMessageOnly sendOtp(@RequestBody SendOTPRequest request) {
        log.info("Controller : " + request);

        otpService.sendOtp(request.getUserId(), request.getEmail());

        return new ApiResponseMessageOnly(200, "OK", "OTP sent successfully to " + request.getEmail());
    }

    @PostMapping("/verify/user")
    public ApiResponseMessageOnly postMethodName(@RequestBody VerifiedUserRequest request) {
        log.info("Controller : " + request);

        String message = otpService.verifiedUser(request);

        return new ApiResponseMessageOnly(HttpStatus.OK.value(), HttpStatus.OK.name(), message);
    }

}
