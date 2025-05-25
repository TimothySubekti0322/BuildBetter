package com.buildbetter.consultation.dto.architect;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeArchitectPasswordRequest {
    @NotBlank(message = "Field 'oldPassword' is required")
    private String oldPassword;
    @NotBlank(message = "Field 'newPassword' is required")
    private String newPassword;
    @NotBlank(message = "Field 'confirmNewPassword' is required")
    private String confirmNewPassword;
}
