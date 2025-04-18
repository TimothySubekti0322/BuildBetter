package com.buildbetter.plan.dto.materials;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialResponse {
    private UUID id;
    private String name;
    private String category;
    private String subCategory;
    private String image;
}
