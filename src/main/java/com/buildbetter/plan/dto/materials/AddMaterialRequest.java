package com.buildbetter.plan.dto.materials;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddMaterialRequest {

    @NotBlank(message = "Field 'name' is required")
    String name;

    @NotBlank(message = "Field 'category' is required")
    String category;

    @NotBlank(message = "Field 'subCategory' is required")
    String subCategory;

    @NotNull(message = "Field 'image' is required")
    MultipartFile image;
}
