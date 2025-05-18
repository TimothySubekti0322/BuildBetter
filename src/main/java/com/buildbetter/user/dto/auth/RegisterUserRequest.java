package com.buildbetter.user.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterUserRequest {

    @NotBlank(message = "Field 'phoneNumber' is required")
    @Pattern(regexp = "^\\+?[0-9]{1,3}[\\s-]?\\d{10,15}$", message = "Phone number must be valid and contain 10 to 15 digits")
    private String phoneNumber;

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

    @NotBlank(message = "Field 'password' is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;
}
