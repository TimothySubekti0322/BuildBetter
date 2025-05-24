package com.buildbetter.consultation.dto.architect;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateArchitectRequest {

    private String username;
    private String province;
    private String city;
    private String photo;

    @Pattern(regexp = "^\\+?[0-9]{1,3}[\\s-]?\\d{10,15}$", message = "Phone number must be valid and contain 10 to 15 digits")
    private String phoneNumber;

    @Min(value = 0, message = "Field 'experience' must be greater than or equal to 0")
    private Float experience;

    @Min(value = 0, message = "Field 'rateOnline' must be greater than or equal to 0")
    private Integer rateOnline;

    @Min(value = 0, message = "Field 'rateOffline' must be greater than or equal to 0")
    private Integer rateOffline;
    private String portfolio;

    private String password;

}
