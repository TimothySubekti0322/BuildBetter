package com.buildbetter.consultation.dto.architect;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterArchitectRequest {
    @NotBlank(message = "Field 'email' is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Field 'username' is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Field 'province' is required")
    private String province;

    @NotBlank(message = "Field 'city' is required")
    private String city;
}
