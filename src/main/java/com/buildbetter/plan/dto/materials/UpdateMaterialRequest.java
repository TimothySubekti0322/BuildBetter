package com.buildbetter.plan.dto.materials;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMaterialRequest {
    String name;
    String category;
    String subCategory;
    MultipartFile image;
}
