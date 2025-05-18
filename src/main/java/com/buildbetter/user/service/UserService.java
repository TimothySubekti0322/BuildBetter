package com.buildbetter.user.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.buildbetter.user.dto.auth.GetUserResponse;
import com.buildbetter.user.dto.user.UpdateUserRequest;
import com.buildbetter.user.model.User;
import com.buildbetter.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public List<User> getAllUsers() {
        log.info("Service : getAllUsers");

        return userRepository.findAll();
    }

    public GetUserResponse getCurrentUser(UUID userId) {
        log.info("Service : getCurrentUser");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        GetUserResponse response = GetUserResponse.builder()
                .id(user.getId())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .username(user.getUsername())
                .province(user.getProvince())
                .city(user.getCity())
                .photo(user.getPhoto())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();

        return response;
    }

    public void updateUser(UUID userId, UpdateUserRequest request) {
        log.info("User Service : updateUser");
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        existingUser.setPhoneNumber(
                request.getPhoneNumber() != null ? request.getPhoneNumber() : existingUser.getPhoneNumber());
        existingUser.setEmail(
                request.getEmail() != null ? request.getEmail() : existingUser.getEmail());
        existingUser.setUsername(
                request.getUsername() != null ? request.getUsername() : existingUser.getUsername());
        existingUser.setProvince(
                request.getProvince() != null ? request.getProvince() : existingUser.getProvince());
        existingUser.setCity(
                request.getCity() != null ? request.getCity() : existingUser.getCity());
        existingUser.setPhoto(
                request.getPhoto() != null ? request.getPhoto() : existingUser.getPhoto());

        log.info("User Service : updateUser - User updated successfully");
        userRepository.save(existingUser);
    }

    {

    }
}
