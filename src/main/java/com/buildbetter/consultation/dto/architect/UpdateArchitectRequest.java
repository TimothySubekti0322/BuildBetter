package com.buildbetter.consultation.dto.architect;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateArchitectRequest {

    String username;
    String province;
    String city;

    MultipartFile photo;

    @Pattern(regexp = "^\\+?[0-9]{1,3}[\\s-]?\\d{10,15}$", message = "Phone number must be valid and contain 10 to 15 digits")
    String phoneNumber;

    @Min(value = 0, message = "Field 'experience' must be greater than or equal to 0")
    Float experience;

    @Min(value = 0, message = "Field 'rateOnline' must be greater than or equal to 0")
    Integer rateOnline;

    @Min(value = 0, message = "Field 'rateOffline' must be greater than or equal to 0")
    Integer rateOffline;
    String portfolio;
}
