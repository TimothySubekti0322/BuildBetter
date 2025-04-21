package com.buildbetter.plan.dto.suggestions;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSuggestionRequest {
    private String houseNumber;
    private int landArea;
    private int buildingArea;
    private String style;
    private int floor;
    private int rooms;
    private int buildingHeight;
    private String designer;
    private int defaultBudget;
    private List<Integer> budgetMin;
    private List<Integer> budgetMax;
    private List<UUID> materials0;
    private List<UUID> materials1;
    private List<UUID> materials2;
}
