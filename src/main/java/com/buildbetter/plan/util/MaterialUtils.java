package com.buildbetter.plan.util;

import com.buildbetter.plan.dto.materials.MaterialResponse;
import com.buildbetter.plan.model.Material;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class MaterialUtils {
    public static MaterialResponse toMaterialResponse(Material material) {
        return MaterialResponse.builder()
                .id(material.getId())
                .name(material.getName())
                .category(material.getCategory())
                .subCategory(material.getSubCategory())
                .image(material.getImage())
                .build();
    }
}
