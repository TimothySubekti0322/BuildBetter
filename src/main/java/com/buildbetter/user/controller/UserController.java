package com.buildbetter.user.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.buildbetter.shared.dto.ApiResponseWithData;
import com.buildbetter.user.model.User;
import com.buildbetter.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/")
    public ApiResponseWithData<List<User>> getAllUsers() {
        log.info("Controller : Get all users");

        List<User> users = userService.getAllUsers();

        return new ApiResponseWithData<>(HttpStatus.OK.value(), HttpStatus.OK.name(), users);
    }
}
