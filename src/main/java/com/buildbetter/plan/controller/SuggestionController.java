package com.buildbetter.plan.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.buildbetter.plan.dto.suggestions.AddSuggestionRequest;
import com.buildbetter.plan.dto.suggestions.AddSuggestionUrlRequest;
import com.buildbetter.plan.dto.suggestions.SuggestionResponse;
import com.buildbetter.plan.dto.suggestions.UpdateSuggestionRequest;
import com.buildbetter.plan.dto.suggestions.UploadFloorPlans;
import com.buildbetter.plan.dto.suggestions.UploadHouseFileRequest;
import com.buildbetter.plan.dto.suggestions.generate.GenerateSuggestionRequest;
import com.buildbetter.plan.dto.suggestions.generate.GenerateSuggestionResponse;
import com.buildbetter.plan.service.SuggestionService;
import com.buildbetter.shared.dto.ApiResponseMessageAndData;
import com.buildbetter.shared.dto.ApiResponseMessageOnly;
import com.buildbetter.shared.dto.ApiResponseWithData;
import com.buildbetter.shared.security.annotation.IsAdmin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/suggestions")
@Slf4j
public class SuggestionController {
    private final SuggestionService suggestionService;

    @PostMapping(path = "")
    @IsAdmin
    public ApiResponseMessageAndData<UUID> addSuggestions(@Valid @RequestBody AddSuggestionRequest request) {
        log.info("Suggestion Controller : addSuggestions");

        UUID new_record_id = suggestionService.addSugesstion(request);

        ApiResponseMessageAndData<UUID> response = new ApiResponseMessageAndData<>();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setMessage("Suggestions added successfully");
        response.setData(new_record_id);

        return response;
    }

    @PostMapping(path = "/upload-floorplans", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @IsAdmin
    public ApiResponseMessageOnly uploadFloorPlans(@Valid @ModelAttribute UploadFloorPlans request) {
        log.info("Suggestion Controller : uploadFloorPlans");

        suggestionService.uploadFloorPlans(request);

        ApiResponseMessageOnly response = new ApiResponseMessageOnly();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setMessage("floorplans files uploaded successfully");

        return response;
    }

    @PostMapping(path = "/upload-file", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @IsAdmin
    public ApiResponseMessageOnly uploadHouseFile(@Valid @ModelAttribute UploadHouseFileRequest request) {
        log.info("Suggestion Controller : uploadHouseFile");

        suggestionService.uploadHouseFile(request);

        ApiResponseMessageOnly response = new ApiResponseMessageOnly();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setMessage(request.getType() + " file uploaded successfully");

        return response;
    }

    @PostMapping("/add-url")
    @IsAdmin
    public ApiResponseMessageOnly addSugesstionUrl(@Valid @RequestBody AddSuggestionUrlRequest request) {
        log.info("Suggestion Controller : addSugesstionUrl");

        suggestionService.addSugesstionUrl(request);

        ApiResponseMessageOnly response = new ApiResponseMessageOnly();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setMessage("URL for " + request.getType() + " added successfully");

        return response;
    }

    @GetMapping("")
    @IsAdmin
    public ApiResponseWithData<List<SuggestionResponse>> getAllSuggestions() {
        log.info("Suggestion Controller : getAllSuggestions");

        List<SuggestionResponse> suggestions = suggestionService.getAllSuggestions();

        ApiResponseWithData<List<SuggestionResponse>> response = new ApiResponseWithData<>();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setData(suggestions);

        return response;
    }

    @GetMapping("/{id}")
    @IsAdmin
    public ApiResponseWithData<SuggestionResponse> getSuggestionById(@PathVariable UUID id) {
        log.info("Suggestion Controller : getSuggestionById");

        SuggestionResponse suggestion = suggestionService.getSuggestionById(id);

        ApiResponseWithData<SuggestionResponse> response = new ApiResponseWithData<>();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setData(suggestion);

        return response;
    }

    @PatchMapping("/{id}")
    @IsAdmin
    public ApiResponseMessageOnly updateSuggestion(@PathVariable UUID id,
            @RequestBody UpdateSuggestionRequest request) {
        log.info("Suggestion Controller : updateSuggestion");

        suggestionService.updateSuggestion(id, request);

        ApiResponseMessageOnly response = new ApiResponseMessageOnly();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setMessage("Suggestion deleted successfully");

        return response;
    }

    @DeleteMapping(path = "/{id}")
    @IsAdmin
    public ApiResponseMessageOnly deleteSuggestion(@PathVariable UUID id) {
        log.info("Suggestion Controller : deleteSuggestion");

        suggestionService.deleteSuggestion(id);

        ApiResponseMessageOnly response = new ApiResponseMessageOnly();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setMessage("Suggestion deleted successfully");

        return response;
    }

    @PostMapping(path = "/generate")
    public ApiResponseMessageAndData<GenerateSuggestionResponse> generateSuggestions(
            @Valid @RequestBody GenerateSuggestionRequest request) {
        GenerateSuggestionResponse result = suggestionService.generateSuggestion(request);
        ApiResponseMessageAndData<GenerateSuggestionResponse> response = new ApiResponseMessageAndData<>();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setMessage("Suggestions generated successfully");
        response.setData(result);
        return response;
    }
}
