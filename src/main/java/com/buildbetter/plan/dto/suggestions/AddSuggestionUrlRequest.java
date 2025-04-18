package com.buildbetter.plan.dto.suggestions;

import java.util.UUID;

import com.buildbetter.plan.validation.ValidHouseFileType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddSuggestionUrlRequest {

    @NotBlank(message = "Field 'url' is required")
    private String url;

    @NotNull(message = "Field 'id' is required")
    private UUID id;

    @NotBlank(message = "Field 'type' is required")
    @ValidHouseFileType
    private String type;
}
