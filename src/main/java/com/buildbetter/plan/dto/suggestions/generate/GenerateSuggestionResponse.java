package com.buildbetter.plan.dto.suggestions.generate;

import com.buildbetter.plan.dto.suggestions.SuggestionResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerateSuggestionResponse {

    GenerateSuggestionRequest userInput;
    SuggestionResponse[] suggestions;

}