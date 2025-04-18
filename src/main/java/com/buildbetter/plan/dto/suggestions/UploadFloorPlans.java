package com.buildbetter.plan.dto.suggestions;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadFloorPlans {

    @NotEmpty(message = "Field 'files' is required")
    MultipartFile[] files;

    @NotNull(message = "Field 'id' is required")
    UUID id;
}
