package com.buildbetter.plan.dto.suggestions;

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
public class GenerateSuggestionRequest {
    @NotBlank(message = "Field 'style' is required")
    private String style;

    @NotNull(message = "Field 'landArea' is required")
    @Positive(message = "Field 'length' must be positive")
    private int landArea;

    @NotNull(message = "Field 'floor' is required")
    @Min(value = 1, message = "Field 'floor' must be at least 1")
    private int floor;
}
