package com.buildbetter.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.buildbetter.shared.dto.ApiResponseMessageOnly;
import com.buildbetter.user.dto.RegisterUserRequest;
import com.buildbetter.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Slf4j
public class UserController {

    @Autowired
    private final UserService userService;

    @PostMapping("/register")
    public ApiResponseMessageOnly registerUser(@RequestBody RegisterUserRequest request) {
        log.info("Controller : " + request);
        String message = userService.register(request);
        return new ApiResponseMessageOnly(HttpStatus.CREATED.value(), HttpStatus.CREATED.name(), message);
    }

    // @PostMapping("/login")
    // public ApiResponseWithData<LoginResponse> loginUser(@RequestBody LoginRequest
    // request) {
    // log.info("Controller : " + request);
    // LoginResponse loginResponse = userService.login(request);
    // return new ApiResponseWithData<>(HttpStatus.OK.value(), HttpStatus.OK.name(),
    // loginResponse);
    // }
}
