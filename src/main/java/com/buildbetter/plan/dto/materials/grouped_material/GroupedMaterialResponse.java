package com.buildbetter.plan.dto.materials.grouped_material;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupedMaterialResponse {
    private String category;
    private SubCategory[] subCategories;
}