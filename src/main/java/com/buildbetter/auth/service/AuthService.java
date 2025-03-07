package com.buildbetter.auth.service;

import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import com.buildbetter.auth.dto.LoginRequest;
import com.buildbetter.auth.dto.LoginResponse;
import com.buildbetter.auth.repository.OtpRepository;
import com.buildbetter.shared.exception.UnauthorizedException;
import com.buildbetter.user.UserAPI;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final OtpRepository otpRepository;
    private final UserAPI userAPI;

    // Login Method here
    public LoginResponse login(LoginRequest loginRequest) {

        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        // Check if user exists
        UUID userId = userAPI.getUserIdByEmail(email);

        if (userId == null) {
            throw new UnauthorizedException("User not found ");
        }

        String pwd = userAPI.getUserPassword(userId);

        // Check if password is correct
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        if (!BCrypt.checkpw(password, hashedPassword)) {
            throw new UnauthorizedException("Invalid password");
        }

        // Generate JWT token
        String token = "dummyToken";

        // Build Response
        return new LoginResponse(
                userId.toString(),
                email,
                token,
                "user",
                "dummyName");
    }
}
