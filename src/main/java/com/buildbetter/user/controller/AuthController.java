package com.buildbetter.user.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.buildbetter.shared.dto.ApiResponseMessageAndData;
import com.buildbetter.shared.dto.ApiResponseMessageOnly;
import com.buildbetter.shared.dto.ApiResponseWithData;
import com.buildbetter.user.dto.auth.ForgotPasswordRequest;
import com.buildbetter.user.dto.auth.LoginRequest;
import com.buildbetter.user.dto.auth.LoginResponse;
import com.buildbetter.user.dto.auth.RegisterUserRequest;
import com.buildbetter.user.dto.auth.ResetPasswordRequest;
import com.buildbetter.user.dto.auth.SendOTPRequest;
import com.buildbetter.user.dto.auth.VerifiedUserRequest;
import com.buildbetter.user.service.AuthService;
import com.buildbetter.user.service.ForgotPasswordService;
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
    private final ForgotPasswordService forgotPassword;

    @GetMapping("/ping")
    public ApiResponseMessageOnly getHello() {
        ApiResponseMessageOnly response = new ApiResponseMessageOnly();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setMessage("Hello from Auth Controller");
        return response;
    }

    @GetMapping("/reset-password-redirect")
    public ResponseEntity<String> landingPage(
            @RequestParam String token,
            @RequestParam String email) {

        String appUrl = String.format(
                "myapp://update-password?token=%s&email=%s",
                URLEncoder.encode(token, StandardCharsets.UTF_8),
                URLEncoder.encode(email, StandardCharsets.UTF_8));
        String downloadUrl = "https://play.google.com/store/apps/details?id=com.buildbetter";
        String html = """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="utf-8">
                  <title>Opening App…</title>
                  <script>
                    // 1. try to open the app
                    window.location = "%1$s";
                    // 2. if still here after 2s, show fallback UI
                    setTimeout(function() {
                      document.getElementById('fallback').style.display = 'block';
                    }, 2000);
                  </script>
                  <style>
                    body { font-family: Arial, sans-serif; text-align: center; padding: 50px; }
                    #fallback { display: none; margin-top: 20px; }
                  </style>
                </head>
                <body>
                  <h1>Opening in your app…</h1>
                  <div id="fallback">
                    <p>If nothing happened, <a href="%2$s" target="_blank">download the app</a> or <a href="%1$s">try again</a>.</p>
                  </div>
                </body>
                </html>
                """
                .formatted(appUrl, downloadUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_HTML);
        return new ResponseEntity<>(html, headers, HttpStatus.OK);
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
}
