package com.buildbetter.user.dto.user;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    @Pattern(regexp = "^\\+?[0-9]{1,3}[\\s-]?\\d{10,15}$", message = "Phone number must be valid and contain 10 to 15 digits")
    private String phoneNumber;
    private String email;
    private String username;
    private String province;
    private String city;
    private String photo;
}
