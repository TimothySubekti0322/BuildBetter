package com.buildbetter.user.service;

import java.util.List;

import org.springframework.stereotype.Service;

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
}
