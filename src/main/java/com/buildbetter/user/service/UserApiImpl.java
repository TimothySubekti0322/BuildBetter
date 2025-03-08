package com.buildbetter.user.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.buildbetter.user.UserAPI;
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
}
