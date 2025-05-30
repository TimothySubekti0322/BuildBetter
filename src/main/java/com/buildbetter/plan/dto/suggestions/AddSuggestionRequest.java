package com.buildbetter.plan.dto.suggestions;

import java.util.List;
import java.util.UUID;

import com.buildbetter.plan.validation.annotation.ValidWindDirection;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddSuggestionRequest {

    @NotBlank(message = "Field 'houseNumber' is required")
    private String houseNumber;

    @NotNull(message = "Field 'windDirection' is required")
    @Size(min = 1, message = "Field 'windDirection' must contain at least one value")
    @ValidWindDirection
    private List<String> windDirection;

    // ── dimensions ─────────────────────────────────
    @NotNull(message = "Field 'landArea' is required")
    private int landArea;
    @NotNull(message = "Field 'buildingArea' is required")
    private int buildingArea;

    // ── style ──────────────────────────────────────
    @NotBlank(message = "Field 'style' is required")
    private String style;
    @NotNull(message = "Field 'floor' is required")
    private int floor;
    @NotNull(message = "Field 'rooms' is required")
    private int rooms;
    @NotNull(message = "Field 'buildingHeight' is required")
    private int buildingHeight;
    @NotBlank(message = "Field 'designer' is required")
    private String designer;

    // ── budgeting ─────────────────────────────────
    @NotNull(message = "Field 'defaultBudget' is required")
    private int defaultBudget;

    @NotNull(message = "Field 'budgetMin' is required")
    @Size(min = 1, message = "Field 'budgetMin' must contain at least one value")
    private List<Integer> budgetMin;

    @NotNull(message = "Field 'budgetMax' is required")
    @Size(min = 1, message = "Field 'budgetMax' must contain at least one value")
    private List<Integer> budgetMax;

    // ── material IDs ─────────────────────────────────────────
    @NotNull(message = "Field 'materials0' is required")
    @Size(min = 1, message = "Field 'materials0' must contain at least one item")
    private List<UUID> materials0;

    @NotNull(message = "Field 'materials1' is required")
    @Size(min = 1, message = "Field 'materials1' must contain at least one item")
    private List<UUID> materials1;

    @NotNull(message = "Field 'materials2' is required")
    @Size(min = 1, message = "Field 'materials2' must contain at least one item")
    private List<UUID> materials2;
}
