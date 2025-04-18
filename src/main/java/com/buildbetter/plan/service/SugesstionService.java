package com.buildbetter.plan.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.buildbetter.plan.constant.HouseFileType;
import com.buildbetter.plan.dto.suggestions.AddSuggestionRequest;
import com.buildbetter.plan.dto.suggestions.AddSuggestionUrlRequest;
import com.buildbetter.plan.dto.suggestions.SuggestionResponse;
import com.buildbetter.plan.dto.suggestions.UploadFloorPlans;
import com.buildbetter.plan.dto.suggestions.UploadHouseFileRequest;
import com.buildbetter.plan.model.Suggestion;
import com.buildbetter.plan.repository.SuggestionRepository;
import com.buildbetter.shared.constant.S3Folder;
import com.buildbetter.shared.exception.NotFoundException;
import com.buildbetter.shared.util.S3Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SugesstionService {
    private final SuggestionRepository suggestionRepository;
    private final S3Service s3Service;

    public void addSugesstion(AddSuggestionRequest request) {

        Suggestion suggestion = new Suggestion();

        suggestion.setHouseNumber(request.getHouseNumber());
        suggestion.setLandArea(request.getLandArea());
        suggestion.setBuildingArea(request.getBuildingArea());
        suggestion.setStyle(request.getStyle());
        suggestion.setFloor(request.getFloor());
        suggestion.setRooms(request.getRooms());
        suggestion.setBuildingHeight(request.getBuildingHeight());
        suggestion.setDesigner(request.getDesigner());
        suggestion.setDefaultBudget(request.getDefaultBudget());
        suggestion.setBudgetMin(request.getBudgetMin());
        suggestion.setBudgetMax(request.getBudgetMax());
        suggestion.setMaterials0(request.getMaterials0());
        suggestion.setMaterials1(request.getMaterials1());
        suggestion.setMaterials2(request.getMaterials2());

        suggestionRepository.save(suggestion);
    }

    public void uploadFloorPlans(UploadFloorPlans request) {
        Suggestion suggestion = suggestionRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException("Suggestion not found"));
        String folder = S3Folder.SUGGESTIONS + suggestion.getHouseNumber() + "/";

        List<String> floorPlansList = suggestion.getFloorplans() != null ? suggestion.getFloorplans()
                : new ArrayList<>();

        // Upload FloorPlan
        for (MultipartFile floorplan : request.getFiles()) {
            if (floorplan.isEmpty())
                continue;

            String imageUrl = s3Service.uploadFile(floorplan, folder);
            floorPlansList.add(imageUrl);
        }

        suggestion.setFloorplans(floorPlansList);

        suggestionRepository.save(suggestion);
    }

    public void uploadHouseFile(UploadHouseFileRequest request) {
        Suggestion suggestion = suggestionRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException("Suggestion not found"));

        String folder = S3Folder.SUGGESTIONS + suggestion.getHouseNumber() + "/";

        // Upload House Image Object
        String houseImageObjectUrl = s3Service.uploadFile(request.getFile(), folder);

        HouseFileType houseFileType = HouseFileType.fromValueIgnoreCase(request.getType());
        // Set the house image based on the type
        if (houseFileType == HouseFileType.HOUSE_IMAGE_FRONT) {
            suggestion.setHouseImageFront(houseImageObjectUrl);
        } else if (houseFileType == HouseFileType.HOUSE_IMAGE_BACK) {
            suggestion.setHouseImageBack(houseImageObjectUrl);
        } else if (houseFileType == HouseFileType.HOUSE_IMAGE_SIDE) {
            suggestion.setHouseImageSide(houseImageObjectUrl);
        } else if (houseFileType == HouseFileType.HOUSE_OBJECT) {
            suggestion.setObject(houseImageObjectUrl);
        }

        suggestionRepository.save(suggestion);
    }

    public void addSugesstionUrl(AddSuggestionUrlRequest request) {
        Suggestion suggestion = suggestionRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException("Suggestion not found"));

        HouseFileType houseFileType = HouseFileType.fromValueIgnoreCase(request.getType());
        // Set the house image based on the type
        if (houseFileType == HouseFileType.HOUSE_IMAGE_FRONT) {
            suggestion.setHouseImageFront(request.getUrl());
        } else if (houseFileType == HouseFileType.HOUSE_IMAGE_BACK) {
            suggestion.setHouseImageBack(request.getUrl());
        } else if (houseFileType == HouseFileType.HOUSE_IMAGE_SIDE) {
            suggestion.setHouseImageSide(request.getUrl());
        } else if (houseFileType == HouseFileType.HOUSE_OBJECT) {
            suggestion.setObject(request.getUrl());
        }

        suggestionRepository.save(suggestion);
    }

    public List<SuggestionResponse> getAllSuggestions() {
        return suggestionRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private SuggestionResponse toResponse(Suggestion suggestion) {
        return SuggestionResponse.builder()
                .id(suggestion.getId())
                .houseNumber(suggestion.getHouseNumber())
                .landArea(suggestion.getLandArea())
                .buildingArea(suggestion.getBuildingArea())
                .style(suggestion.getStyle())
                .floor(suggestion.getFloor())
                .rooms(suggestion.getRooms())
                .buildingHeight(suggestion.getBuildingHeight())
                .designer(suggestion.getDesigner())
                .defaultBudget(suggestion.getDefaultBudget())
                .budgetMin(suggestion.getBudgetMin())
                .budgetMax(suggestion.getBudgetMax())
                .floorplans(suggestion.getFloorplans())
                .object(suggestion.getObject())
                .houseImageFront(suggestion.getHouseImageFront())
                .houseImageBack(suggestion.getHouseImageBack())
                .houseImageSide(suggestion.getHouseImageSide())
                .materials0(suggestion.getMaterials0())
                .materials1(suggestion.getMaterials1())
                .materials2(suggestion.getMaterials2())
                .build();
    }
}
