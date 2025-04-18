package com.buildbetter.plan.dto.materials.grouped_material;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubCategory {
    private String subCategory;
    private MaterialItem[] materials;
}
