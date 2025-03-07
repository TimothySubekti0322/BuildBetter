package com.buildbetter.user.dto;

import lombok.Data;

@Data
public class RegisterUserRequest {
    private String phoneNumber;
    private String email;
    private String username;
    private String province;
    private String city;
    private String password;
}
