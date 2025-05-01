package com.buildbetter.plan.dto.plans;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddPlanRequest {
    @NotBlank(message = "Field 'province' is required")
    private String province;
    @NotBlank(message = "Field 'city' is required")
    private String city;

    /* ──────────── land details ──────────── */
    @NotBlank(message = "Field 'landform' is required")
    private String landform;

    @NotNull(message = "Field 'landArea' is required")
    @Positive(message = "Field 'landArea' must be positive")
    private int landArea;

    @NotBlank(message = "Field 'entranceDirection' is required")
    private String entranceDirection;

    /* ──────────── building spec ─── */
    @NotBlank(message = "Field 'style' is required")
    private String style;

    @NotNull(message = "Field 'floor' is required")
    @Min(value = 1, message = "Field 'floor' must be at least 1")
    private int floor;

    @NotNull(message = "Field 'rooms' is required")
    @Min(value = 1, message = "Field 'rooms' must be at least 1")
    private int rooms;

    @NotNull(message = "Field 'suggestionId' is required")
    private UUID suggestionId;
}