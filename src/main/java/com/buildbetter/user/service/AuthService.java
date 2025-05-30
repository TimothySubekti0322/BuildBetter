package com.buildbetter.user.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import com.buildbetter.shared.exception.BadRequestException;
import com.buildbetter.shared.util.JwtUtil;
import com.buildbetter.user.dto.auth.LoginRequest;
import com.buildbetter.user.dto.auth.LoginResponse;
import com.buildbetter.user.dto.auth.RegisterUserRequest;
import com.buildbetter.user.dto.auth.SendOTPRequest;
import com.buildbetter.user.dto.auth.VerifiedUserRequest;
import com.buildbetter.user.model.User;
import com.buildbetter.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final JwtUtil jwtUtil;

    // Register User
    public String registerUser(RegisterUserRequest request) {
        log.info("Auth Service : registerUser");

        // Check if user already exists
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            throw new BadRequestException("User already exists");
        });

        // TODO: DECIDE IF WE NEED TO CHECK FOR PHONE NUMBER
        // userRepository.findByPhoneNumber(request.getPhoneNumber()).ifPresent(user ->
        // {
        // throw new BadRequestException("User already exists with phone number: " +
        // request.getPhoneNumber());
        // });

        // Hash password
        log.info("Auth Service : Hashing password");
        String hashedPassword = BCrypt.hashpw(request.getPassword(), BCrypt.gensalt());

        User user = User.builder()
                // id is generated in @PrePersist model
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .username(request.getUsername())
                .province(request.getProvince())
                .city(request.getCity())
                .password(hashedPassword)
                .role("user")
                .isVerified(false)
                .createdAt(LocalDateTime.now())
                .build();

        log.info("Auth Service : Saving user to DB");
        User savedUser = userRepository.save(user);

        // Send email Verification link
        log.info("Auth Service : Sending OTP to email");
        otpService.sendOtp(new SendOTPRequest(savedUser.getEmail()));

        return savedUser.getId().toString();
    }

    // Verify User
    public String verifyUser(VerifiedUserRequest request) {
        log.info("Auth Service : verifyUser");

        String email = request.getEmail();
        String otp = request.getOtp();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not registered"));

        // Check if user is already verified
        if (user.getIsVerified()) {
            throw new BadRequestException("User already verified");
        }

        // Verify OTP
        log.info("Auth Service : Verifying OTP");
        if (!otpService.verifyOtp(user.getId(), otp)) {
            throw new BadRequestException("Invalid OTP");
        }

        log.info("Auth Service : OTP verified successfully");
        user.setIsVerified(true);
        userRepository.save(user);

        return "User Verified Successfully";
    }

    // Login
    public LoginResponse login(LoginRequest request) {
        log.info("Auth Service : login");

        String email = request.getEmail();
        String password = request.getPassword();

        // Check if user exists
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            throw new BadRequestException("User not registered");
        } else if (!user.getIsVerified()) {
            throw new BadRequestException("User not verified");
        }

        // Check if password is correct
        log.info("Auth Service : Checking password");
        if (!BCrypt.checkpw(password, user.getPassword())) {
            throw new BadRequestException("Invalid password");
        }

        // Generate JWT token
        log.info("Auth Service : Generating JWT token");
        String token = jwtUtil.generateToken(user.getId().toString(), user.getUsername(), user.getRole());

        // Build Response
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setUserId(user.getId().toString());
        loginResponse.setEmail(email);
        loginResponse.setToken(token);
        loginResponse.setRole(user.getRole());
        loginResponse.setUsername(user.getUsername());
        loginResponse.setCity(user.getCity());

        return loginResponse;
    }
}
