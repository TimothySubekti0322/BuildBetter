package com.buildbetter.plan.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.buildbetter.plan.constant.HouseFileType;
import com.buildbetter.plan.dto.suggestions.AddSuggestionRequest;
import com.buildbetter.plan.dto.suggestions.AddSuggestionUrlRequest;
import com.buildbetter.plan.dto.suggestions.GenerateSuggestionRequest;
import com.buildbetter.plan.dto.suggestions.GenerateSuggestionResponse;
import com.buildbetter.plan.dto.suggestions.SuggestionResponse;
import com.buildbetter.plan.dto.suggestions.UpdateSuggestionRequest;
import com.buildbetter.plan.dto.suggestions.UploadFloorPlans;
import com.buildbetter.plan.dto.suggestions.UploadHouseFileRequest;
import com.buildbetter.plan.model.Material;
import com.buildbetter.plan.model.Suggestion;
import com.buildbetter.plan.repository.MaterialRepository;
import com.buildbetter.plan.repository.SuggestionRepository;
import com.buildbetter.plan.util.SuggestionUtils;
import com.buildbetter.shared.constant.S3Folder;
import com.buildbetter.shared.exception.NotFoundException;
import com.buildbetter.shared.util.S3Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SuggestionService {

    private final MaterialRepository materialRepository;
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

        if (suggestion.getFloorplans() != null) {
            for (String floorPlan : suggestion.getFloorplans()) {
                s3Service.deleteFile(floorPlan);
            }
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
            if (suggestion.getHouseImageFront() != null) {
                s3Service.deleteFile(suggestion.getHouseImageFront());
            }
            suggestion.setHouseImageFront(houseImageObjectUrl);
        } else if (houseFileType == HouseFileType.HOUSE_IMAGE_BACK) {
            if (suggestion.getHouseImageBack() != null) {
                s3Service.deleteFile(suggestion.getHouseImageBack());
            }
            suggestion.setHouseImageBack(houseImageObjectUrl);
        } else if (houseFileType == HouseFileType.HOUSE_IMAGE_SIDE) {
            if (suggestion.getHouseImageSide() != null) {
                s3Service.deleteFile(suggestion.getHouseImageSide());
            }
            suggestion.setHouseImageSide(houseImageObjectUrl);
        } else if (houseFileType == HouseFileType.HOUSE_OBJECT) {
            if (suggestion.getObject() != null) {
                s3Service.deleteFile(suggestion.getObject());
            }
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

        List<Suggestion> suggestions = suggestionRepository.findAll();

        // fetch ALL material ids only once (O(1) DB call)
        Set<UUID> allIds = suggestions.stream()
                .flatMap(s -> Stream.of(s.getMaterials0(),
                        s.getMaterials1(),
                        s.getMaterials2()))
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        Map<UUID, Material> materialById = materialRepository.findAllById(allIds) // one query
                .stream()
                .collect(Collectors.toMap(Material::getId,
                        Function.identity()));

        // map every suggestion
        return suggestions.stream()
                .map(s -> SuggestionUtils.toGetSuggestionResponse(s, materialById))
                .toList();
    }

    @Transactional
    public void updateSuggestion(UUID id, UpdateSuggestionRequest request) {
        Suggestion existingSuggestion = suggestionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Suggestion not found"));

        existingSuggestion.setHouseNumber(
                request.getHouseNumber() != null ? request.getHouseNumber() : existingSuggestion.getHouseNumber());
        existingSuggestion
                .setLandArea(request.getLandArea() != 0 ? request.getLandArea() : existingSuggestion.getLandArea());
        existingSuggestion.setBuildingArea(
                request.getBuildingArea() != 0 ? request.getBuildingArea() : existingSuggestion.getBuildingArea());
        existingSuggestion.setStyle(request.getStyle() != null ? request.getStyle() : existingSuggestion.getStyle());
        existingSuggestion.setFloor(request.getFloor() != 0 ? request.getFloor() : existingSuggestion.getFloor());
        existingSuggestion.setRooms(request.getRooms() != 0 ? request.getRooms() : existingSuggestion.getRooms());
        existingSuggestion.setBuildingHeight(
                request.getBuildingHeight() != 0 ? request.getBuildingHeight()
                        : existingSuggestion.getBuildingHeight());
        existingSuggestion
                .setDesigner(request.getDesigner() != null ? request.getDesigner() : existingSuggestion.getDesigner());
        existingSuggestion.setDefaultBudget(
                request.getDefaultBudget() != 0 ? request.getDefaultBudget() : existingSuggestion.getDefaultBudget());
        existingSuggestion.setBudgetMin(
                request.getBudgetMin() != null ? request.getBudgetMin() : existingSuggestion.getBudgetMin());
        existingSuggestion.setBudgetMax(
                request.getBudgetMax() != null ? request.getBudgetMax() : existingSuggestion.getBudgetMax());
        existingSuggestion.setMaterials0(
                request.getMaterials0() != null ? request.getMaterials0() : existingSuggestion.getMaterials0());
        existingSuggestion.setMaterials1(
                request.getMaterials1() != null ? request.getMaterials1() : existingSuggestion.getMaterials1());
        existingSuggestion.setMaterials2(
                request.getMaterials2() != null ? request.getMaterials2() : existingSuggestion.getMaterials2());

        suggestionRepository.save(existingSuggestion);
    }

    public void deleteSuggestion(UUID id) {
        Suggestion suggestion = suggestionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Suggestion not found"));

        // Delete all files from S3 bucket
        List<String> floorPlans = suggestion.getFloorplans();
        if (floorPlans != null) {
            for (String floorPlan : floorPlans) {
                s3Service.deleteFile(floorPlan);
            }
        }

        if (suggestion.getHouseImageFront() != null) {
            s3Service.deleteFile(suggestion.getHouseImageFront());
        }
        if (suggestion.getHouseImageBack() != null) {
            s3Service.deleteFile(suggestion.getHouseImageBack());
        }
        if (suggestion.getHouseImageSide() != null) {
            s3Service.deleteFile(suggestion.getHouseImageSide());
        }
        if (suggestion.getObject() != null) {
            s3Service.deleteFile(suggestion.getObject());
        }

        suggestionRepository.deleteById(id);
    }

    public List<GenerateSuggestionResponse> generateSuggestion(GenerateSuggestionRequest req) {

        String style = req.getStyle().trim();
        int landArea = req.getLandArea();
        int floor = req.getFloor();

        /* ── single DB hit ─────────────────────────────────────── */
        List<Suggestion> pool = suggestionRepository.findByStyleIgnoreCase(style);
        if (pool.isEmpty())
            return List.of();

        // ── Rule 1 ─ exact match ───────────────────────────────────────
        List<Suggestion> selected = pool.stream()
                .filter(s -> s.getLandArea() == landArea && s.getFloor() == floor)
                .toList();

        // ── Rule 2 ─ same floor, closest smaller landArea ──────────────
        if (selected.isEmpty()) {
            Optional<Suggestion> opt = pool.stream()
                    .filter(s -> s.getFloor() == floor && s.getLandArea() < landArea)
                    .max(Comparator.comparingInt(Suggestion::getLandArea));

            if (opt.isPresent()) {
                selected = List.of(opt.get());
            }
        }

        // ── Rule 3 ─ same landArea, different floor ────────────────────
        if (selected.isEmpty()) {
            selected = pool.stream()
                    .filter(s -> s.getLandArea() == landArea && s.getFloor() != floor)
                    .toList();
        }

        // ── Rule 4 ─ smaller landArea, different floor ─────────────────
        if (selected.isEmpty()) {
            Optional<Suggestion> opt = pool.stream()
                    .filter(s -> s.getLandArea() < landArea && s.getFloor() != floor)
                    .max(Comparator.comparingInt(Suggestion::getLandArea));

            if (opt.isPresent()) {
                selected = List.of(opt.get());
            }
        }

        // ── nothing matched → bail out before hitting materials table ─
        if (selected.isEmpty()) {
            return List.of();
        }

        /* ── bulk-load materials only once ─────────────────────── */
        Set<UUID> matIds = SuggestionUtils.collectMaterialIds(selected);
        Map<UUID, Material> mats = materialRepository.findAllById(matIds)
                .stream()
                .collect(Collectors.toMap(Material::getId, m -> m));

        /* ── map to DTOs via util ─────────────────────────────── */
        return selected.stream()
                .map(s -> SuggestionUtils.toGenerateSuggestionResponseDto(s, mats))
                .toList();
    }
}
