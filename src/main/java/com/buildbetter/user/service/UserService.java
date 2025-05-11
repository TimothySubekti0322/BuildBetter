package com.buildbetter.user.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.buildbetter.user.dto.GetUserResponse;
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
                .photos(user.getPhotos())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();

        return response;
    }
}
