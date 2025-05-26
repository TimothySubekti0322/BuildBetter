package com.buildbetter.plan.dto.plans;

import java.util.UUID;

import com.buildbetter.plan.dto.suggestions.SuggestionResponse;
import com.buildbetter.plan.dto.suggestions.generate.GenerateSuggestionRequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetPlansResponse {
    UUID id;
    GenerateSuggestionRequest userInput;
    SuggestionResponse suggestions;
}
