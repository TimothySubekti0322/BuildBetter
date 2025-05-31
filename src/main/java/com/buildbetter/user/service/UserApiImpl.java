package com.buildbetter.user.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.buildbetter.user.UserAPI;
import com.buildbetter.user.dto.user.GetUserNameAndCity;
import com.buildbetter.user.model.User;
import com.buildbetter.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserApiImpl implements UserAPI {

    private final UserRepository userRepository;

    @Override
    public boolean existsById(UUID userId) {
        return userRepository.existsById(userId);
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
    public Map<UUID, GetUserNameAndCity> getAllUsersNameAndCity(UUID requestingUserId) {
        log.info("UserApiImpl : getAllUsers - Requesting User ID: {}", requestingUserId);
        if (requestingUserId == null) {
            throw new IllegalArgumentException("Requesting user ID cannot be null");
        }

        User requestingUser = userRepository.findById(requestingUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + requestingUserId));

        if (!requestingUser.getRole().equals("admin")) {
            throw new IllegalArgumentException(
                    "User with ID " + requestingUserId + " is not authorized to view all users");
        }

        List<User> users = userRepository.findAll();

        return users.stream().collect(
                Collectors.toMap(User::getId, u -> new GetUserNameAndCity(u.getId(), u.getUsername(), u.getCity())));
    }

    @Override
    public GetUserNameAndCity getUserNameAndCityById(UUID userId) {
        log.info("UserApiImpl : getUserById - User ID: {}", userId);
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        GetUserNameAndCity userNameAndCity = GetUserNameAndCity.builder()
                .id(user.getId())
                .username(user.getUsername())
                .city(user.getCity())
                .build();

        return userNameAndCity;
    }
}