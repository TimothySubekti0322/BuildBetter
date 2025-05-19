package com.buildbetter.consult.dto.architect;

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

    private Float experience;
    private Integer rateOnline;
    private Integer rateOffline;
    private String portfolio;

}
