package com.buildbetter.plan.dto.suggestions;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.buildbetter.plan.dto.materials.MaterialResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuggestionResponse {

    private UUID id;
    private String houseNumber;

    // ── dimensions ───────────────────────────────────────────
    private int landArea;
    private int buildingArea;
    private String style;
    private int floor;
    private int rooms;
    private int buildingHeight;
    private String designer;

    // ── budgeting ────────────────────────────────────────────
    private int defaultBudget;
    private List<Integer> budgetMin;
    private List<Integer> budgetMax;

    // ── design assets ────────────────────────────────────────
    private List<String> floorplans;
    private String object; // 3‑D object URL

    private String houseImageFront;
    private String houseImageBack;
    private String houseImageSide;

    // ── material IDs ─────────────────────────────────────────
    Map<String, Map<String, MaterialResponse>> materials0;
    Map<String, Map<String, MaterialResponse>> materials1;
    Map<String, Map<String, MaterialResponse>> materials2;
}
