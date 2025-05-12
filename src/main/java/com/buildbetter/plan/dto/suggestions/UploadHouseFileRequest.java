package com.buildbetter.plan.dto.suggestions;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.buildbetter.plan.validation.annotation.ValidHouseFileType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadHouseFileRequest {

    @NotNull(message = "Field 'file' is required")
    MultipartFile file;

    @NotNull(message = "Field 'id' is required")
    UUID id;

    @NotBlank(message = "Field 'type' is required")
    @ValidHouseFileType
    String type;
}
