package com.buildbetter.plan.dto.suggestions.grouped_suggestions;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialItem {
    private UUID id;
    private String name;
    private String category;
    private String subCategory;
    private String image;
}
