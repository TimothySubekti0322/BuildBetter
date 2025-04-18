package com.buildbetter.plan.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.buildbetter.plan.dto.suggestions.AddSuggestionRequest;
import com.buildbetter.plan.dto.suggestions.AddSuggestionUrlRequest;
import com.buildbetter.plan.dto.suggestions.SuggestionResponse;
import com.buildbetter.plan.dto.suggestions.UploadFloorPlans;
import com.buildbetter.plan.dto.suggestions.UploadHouseFileRequest;
import com.buildbetter.plan.service.SugesstionService;
import com.buildbetter.shared.dto.ApiResponseMessageOnly;
import com.buildbetter.shared.dto.ApiResponseWithData;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/suggestions")
@Slf4j
public class SuggestionController {
    private final SugesstionService suggestionService;

    @PostMapping(path = "")
    public ApiResponseMessageOnly addSuggestions(@Valid @RequestBody AddSuggestionRequest request) {
        log.info("Controller : Add suggestion");

        suggestionService.addSugesstion(request);

        ApiResponseMessageOnly response = new ApiResponseMessageOnly();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setMessage("Suggestions added successfully");

        return response;
    }

    @PostMapping(path = "/upload-floorplans", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ApiResponseMessageOnly uploadFloorPlans(@Valid @ModelAttribute UploadFloorPlans request) {
        log.info("Controller : Upload Floor Plans File");

        suggestionService.uploadFloorPlans(request);

        ApiResponseMessageOnly response = new ApiResponseMessageOnly();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setMessage("floorplans files uploaded successfully");

        return response;
    }

    @PostMapping(path = "/upload-file", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ApiResponseMessageOnly uploadHouseFile(@Valid @ModelAttribute UploadHouseFileRequest request) {
        log.info("Controller : Upload House File");

        suggestionService.uploadHouseFile(request);

        ApiResponseMessageOnly response = new ApiResponseMessageOnly();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setMessage(request.getType() + " file uploaded successfully");

        return response;
    }

    @PostMapping("/add-url")
    public ApiResponseMessageOnly addSugesstionUrl(@Valid @RequestBody AddSuggestionUrlRequest request) {
        log.info("Controller : Add suggestion URL");

        suggestionService.addSugesstionUrl(request);

        ApiResponseMessageOnly response = new ApiResponseMessageOnly();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setMessage("URL for " + request.getType() + " added successfully");

        return response;
    }

    @GetMapping("")
    public ApiResponseWithData<?> getAll(@RequestParam(defaultValue = "false") boolean grouped) {
        // if (grouped) {
        // List<SuggestionResponse> groupedMaterials =
        // materialService.getAllGroupedMaterials();
        // return new ApiResponseWithData<>(HttpStatus.OK.value(), HttpStatus.OK.name(),
        // groupedMaterials);
        // } else {
        // List<SuggestionResponse> materials = materialService.getAllMaterials();
        // return new ApiResponseWithData<>(HttpStatus.OK.value(), HttpStatus.OK.name(),
        // materials);
        // }
        List<SuggestionResponse> suggestions = suggestionService.getAllSuggestions();
        return new ApiResponseWithData<>(HttpStatus.OK.value(), HttpStatus.OK.name(), suggestions);
    }
}
