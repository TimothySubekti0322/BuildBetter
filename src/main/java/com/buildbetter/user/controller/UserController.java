package com.buildbetter.user.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.buildbetter.shared.dto.ApiResponseMessageOnly;
import com.buildbetter.shared.dto.ApiResponseWithData;
import com.buildbetter.shared.security.JwtAuthentication;
import com.buildbetter.shared.security.annotation.Authenticated;
import com.buildbetter.shared.security.annotation.IsAdmin;
import com.buildbetter.user.dto.auth.GetUserResponse;
import com.buildbetter.user.dto.user.UpdateUserRequest;
import com.buildbetter.user.model.User;
import com.buildbetter.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/")
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/users")
    @IsAdmin
    public ApiResponseWithData<List<User>> getAllUsers() {
        log.info("User Controller : getAllUsers");

        List<User> users = userService.getAllUsers();

        return new ApiResponseWithData<>(HttpStatus.OK.value(), HttpStatus.OK.name(), users);
    }

    @GetMapping("/me")
    @Authenticated
    public ApiResponseWithData<GetUserResponse> getCurrentUser(Authentication auth) {
        log.info("User Controller : getCurrentUser");

        log.info("User Controller : getCurrentUser - Parse JWT Authentication");
        JwtAuthentication jwt = (JwtAuthentication) auth;
        UUID userId = UUID.fromString(jwt.claim("id"));

        GetUserResponse user = userService.getCurrentUser(userId);

        return new ApiResponseWithData<>(HttpStatus.OK.value(), HttpStatus.OK.name(), user);
    }

    @PatchMapping("/users")
    @Authenticated
    public ApiResponseMessageOnly updateUser(Authentication auth, @Valid @RequestBody UpdateUserRequest request) {
        log.info("User Controller : updateUser");

        log.info("User Controller : updateUser - Parse JWT Authentication");
        JwtAuthentication jwt = (JwtAuthentication) auth;
        UUID userId = UUID.fromString(jwt.claim("id"));

        userService.updateUser(userId, request);

        ApiResponseMessageOnly response = new ApiResponseMessageOnly();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setMessage("User updated successfully");

        return response;
    }
}
