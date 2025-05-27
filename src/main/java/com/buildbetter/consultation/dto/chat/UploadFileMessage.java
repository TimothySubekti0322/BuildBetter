package com.buildbetter.consultation.dto.chat;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadFileMessage {
    @NotNull(message = "Field 'file' is required")
    MultipartFile file; // The file to be uploaded
}
