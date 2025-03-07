package com.buildbetter.user.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import com.buildbetter.auth.AuthAPI;
import com.buildbetter.shared.exception.BadRequestException;
import com.buildbetter.user.UserAPI;
import com.buildbetter.user.dto.RegisterUserRequest;
import com.buildbetter.user.model.User;
import com.buildbetter.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserAPI {

    private final UserRepository userRepository;

    private final AuthAPI authAPI;

    @Override
    public boolean existsById(UUID userId) {
        return userRepository.existsById(userId);
    }

    @Override
    public void verifiedUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found with id: " + userId));

        user.setIsVerified(true);
        userRepository.save(user);
    }

    @Override
    public UUID getUserIdByEmail(String email) {
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return null;
        }

        return user.getId();
    }

    @Override
    public String getUserPassword(UUID userId) {
        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return "";
        }

        return user.getPassword();
    }

    public String register(RegisterUserRequest request) {
        log.info("Service : " + request);
        // Check if user already exists by email or phoneNumber
        userRepository.findByEmail(request.getEmail())
                .ifPresent(u -> {
                    throw new BadRequestException("Email is already registered.");
                });

        userRepository.findByPhoneNumber(request.getPhoneNumber())
                .ifPresent(u -> {
                    throw new BadRequestException("Phone Number is already registered.");
                });

        // Hash password
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

        User savedUser = userRepository.save(user);

        // Send email verification link
        authAPI.sendOtp(savedUser.getId().toString(), savedUser.getEmail());

        return "Email verification link sent to email";
    }
}
