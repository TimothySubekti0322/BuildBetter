package com.buildbetter.user.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifiedUserRequest {

    @NotBlank(message = "Field 'email' is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Field 'otp' is required")
    @Size(min = 6, max = 6, message = "OTP must be 6 characters long")
    private String otp;
}
