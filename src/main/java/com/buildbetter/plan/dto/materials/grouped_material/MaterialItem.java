package com.buildbetter.plan.dto.materials.grouped_material;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialItem {
    private UUID id;
    private String name;
    private String category;
    private String subCategory;
    private String image;
}
