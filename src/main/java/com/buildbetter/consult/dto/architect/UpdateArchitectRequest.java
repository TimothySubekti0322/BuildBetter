package com.buildbetter.consult.dto.architect;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateArchitectRequest {
    private String email;
    private String username;
    private String province;
    private String city;
    private String photo;

    @Pattern(regexp = "^\\+?[0-9]{1,3}[\\s-]?\\d{10,15}$", message = "Phone number must be valid and contain 10 to 15 digits")
    private String phoneNumber;

    @Max(value = 15, message = "Field 'experience' must be less than or equal to 15")
    private Float experience;

    @Min(value = 0, message = "Field 'rateOnline' must be greater than or equal to 0")
    private Integer rateOnline;

    @Min(value = 0, message = "Field 'rateOffline' must be greater than or equal to 0")
    private Integer rateOffline;
    private String portfolio;

}
