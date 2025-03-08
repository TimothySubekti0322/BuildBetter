package com.buildbetter.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {

    @NotBlank(message = "Field 'email' is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Field 'token' is required")
    private String token;

    @NotBlank(message = "Field 'newPassword' is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String newPassword;
}
