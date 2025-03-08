package com.buildbetter.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Field 'email' is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Field 'password' is required")
    private String password;
}
