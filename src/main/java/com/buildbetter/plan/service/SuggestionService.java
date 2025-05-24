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
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.buildbetter.plan.constant.HouseFileType;
import com.buildbetter.plan.dto.suggestions.AddSuggestionRequest;
import com.buildbetter.plan.dto.suggestions.AddSuggestionUrlRequest;
import com.buildbetter.plan.dto.suggestions.SuggestionResponse;
import com.buildbetter.plan.dto.suggestions.UpdateSuggestionRequest;
import com.buildbetter.plan.dto.suggestions.UploadFloorPlans;
import com.buildbetter.plan.dto.suggestions.UploadHouseFileRequest;
import com.buildbetter.plan.dto.suggestions.generate.GenerateSuggestionRequest;
import com.buildbetter.plan.dto.suggestions.generate.GenerateSuggestionResponse;
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

    public UUID addSuggestion(AddSuggestionRequest request) {
        log.info("Suggestion Service : addSugesstion");

        Suggestion suggestion = new Suggestion();

        suggestion.setHouseNumber(request.getHouseNumber());
        suggestion.setWindDirection(request.getWindDirection());
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

        Suggestion saved = suggestionRepository.save(suggestion);

        return saved.getId();
    }

    public void uploadFloorPlans(UploadFloorPlans request) {
        log.info("Suggestion Service : uploadFloorPlans");

        Suggestion suggestion = suggestionRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException("Suggestion not found"));

        String folder = S3Folder.SUGGESTIONS + suggestion.getHouseNumber() + "/";

        List<String> floorPlansList = new ArrayList<>();

        // Upload FloorPlan
        for (MultipartFile floorplan : request.getFiles()) {
            if (floorplan.isEmpty())
                continue;

            log.info("Suggestion Service : uploadFloorPlans - Upload File " + floorplan.getOriginalFilename()
                    + " to S3");
            String imageUrl = s3Service.uploadFile(floorplan, folder, "");
            floorPlansList.add(imageUrl);
        }

        if (suggestion.getFloorplans() != null) {
            for (String floorPlan : suggestion.getFloorplans()) {
                log.info("Suggestion Service : uploadFloorPlans - Delete old floorplan file " + floorPlan + " from S3");
                s3Service.deleteFile(floorPlan);
            }
        }

        suggestion.setFloorplans(floorPlansList);

        log.info("Suggestion Service : uploadFloorPlans - Save updated suggestion to DB");
        suggestionRepository.save(suggestion);
    }

    public void uploadHouseFile(UploadHouseFileRequest request) {
        log.info("Suggestion Service : uploadHouseFile");

        Suggestion suggestion = suggestionRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException("Suggestion not found"));

        String folder = S3Folder.SUGGESTIONS + suggestion.getHouseNumber() + "/";

        // Upload House Image Object
        log.info("Suggestion Service : uploadHouseFile - Upload File " + request.getFile().getOriginalFilename()
                + " to S3");
        String houseImageObjectUrl = s3Service.uploadFile(request.getFile(), folder, "");

        log.info("Suggestion Service : uploadHouseFile - Convert HouseFileType into enum");
        HouseFileType houseFileType = HouseFileType.fromValueIgnoreCase(request.getType());

        // Set the house image based on the type
        if (houseFileType == HouseFileType.HOUSE_IMAGE_FRONT) {
            String houseImage = suggestion.getHouseImageFront();
            if (StringUtils.hasText(houseImage)) {
                log.info("Suggestion Service : uploadHouseFile - Delete old house file "
                        + houseImage + " from S3");
                s3Service.deleteFile(houseImage);
            }
            suggestion.setHouseImageFront(houseImageObjectUrl);
        } else if (houseFileType == HouseFileType.HOUSE_IMAGE_BACK) {
            String houseImage = suggestion.getHouseImageBack();
            if (StringUtils.hasText(houseImage)) {
                log.info("Suggestion Service : uploadHouseFile - Delete old house file "
                        + houseImage + " from S3");
                s3Service.deleteFile(houseImage);
            }
            suggestion.setHouseImageBack(houseImageObjectUrl);
        } else if (houseFileType == HouseFileType.HOUSE_IMAGE_SIDE) {
            String houseImage = suggestion.getHouseImageSide();
            if (StringUtils.hasText(houseImage)) {
                log.info("Suggestion Service : uploadHouseFile - Delete old house file "
                        + houseImage + " from S3");
                s3Service.deleteFile(houseImage);
            }
            suggestion.setHouseImageSide(houseImageObjectUrl);
        } else if (houseFileType == HouseFileType.HOUSE_OBJECT) {
            String houseImage = suggestion.getObject();
            if (StringUtils.hasText(houseImage)) {
                log.info("Suggestion Service : uploadHouseFile - Delete old house file "
                        + houseImage + " from S3");
                s3Service.deleteFile(houseImage);
            }
            suggestion.setObject(houseImageObjectUrl);
        } else if (houseFileType == HouseFileType.PDF) {
            String houseImage = suggestion.getPdf();
            if (StringUtils.hasText(houseImage)) {
                log.info("Suggestion Service : uploadHouseFile - Delete old house file "
                        + houseImage + " from S3");
                s3Service.deleteFile(houseImage);
            }
            suggestion.setPdf(houseImageObjectUrl);
        }

        log.info("Suggestion Service : uploadHouseFile - Save updated suggestion to DB");
        suggestionRepository.save(suggestion);
    }

    public void addSugesstionUrl(AddSuggestionUrlRequest request) {
        log.info("Suggestion Service : addSugesstionUrl");

        Suggestion suggestion = suggestionRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException("Suggestion not found"));

        log.info("Suggestion Service : addSuggestionUrl - Convert HouseFileType into enum");
        HouseFileType houseFileType = HouseFileType.fromValueIgnoreCase(request.getType());

        // Set the house image based on the type
        String url = request.getUrl();
        if (houseFileType == HouseFileType.HOUSE_IMAGE_FRONT) {
            suggestion.setHouseImageFront(url);
        } else if (houseFileType == HouseFileType.HOUSE_IMAGE_BACK) {
            suggestion.setHouseImageBack(url);
        } else if (houseFileType == HouseFileType.HOUSE_IMAGE_SIDE) {
            suggestion.setHouseImageSide(url);
        } else if (houseFileType == HouseFileType.HOUSE_OBJECT) {
            suggestion.setObject(url);
        } else if (houseFileType == HouseFileType.PDF) {
            suggestion.setPdf(url);
        }

        log.info("Suggestion Service : addSugesstionUrl - Save updated suggestion to DB");
        suggestionRepository.save(suggestion);
    }

    public List<SuggestionResponse> getAllSuggestions() {
        log.info("Suggestion Service : getAllSuggestions");

        List<Suggestion> suggestions = suggestionRepository.findAll();

        // fetch ALL material ids
        log.info("Suggestion Service : getAllSuggestions - Fetch all materials from DB");
        Set<UUID> allIds = suggestions.stream()
                .flatMap(s -> Stream.of(s.getMaterials0(),
                        s.getMaterials1(),
                        s.getMaterials2()))
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        log.info("Suggestion Service : getAllSuggestions - Fetch all materials from DB in Map<UUID, Material> format");
        Map<UUID, Material> materialById = materialRepository.findAllById(allIds) // one query
                .stream()
                .collect(Collectors.toMap(Material::getId,
                        Function.identity()));

        log.info("Suggestion Service : getAllSuggestions - Map every suggestion to SuggestionResponse");
        return suggestions.stream()
                .map(s -> SuggestionUtils.toGetSuggestionResponse(s, materialById))
                .toList();
    }

    public SuggestionResponse getSuggestionById(UUID id) {
        log.info("Suggestion Service : getSuggestionById");

        Suggestion suggestion = suggestionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Suggestion not found"));

        // fetch ALL material ids
        log.info("Suggestion Service : getSuggestionById - Fetch all materials from DB");
        Set<UUID> allIds = Stream.of(suggestion.getMaterials0(),
                suggestion.getMaterials1(),
                suggestion.getMaterials2())
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        log.info("Suggestion Service : getSuggestionById - Fetch all materials from DB in Map<UUID, Material> format");
        Map<UUID, Material> materialById = materialRepository.findAllById(allIds) // one query
                .stream()
                .collect(Collectors.toMap(Material::getId,
                        Function.identity()));

        log.info("Suggestion Service : getSuggestionById - Map suggestion to SuggestionResponse");
        return SuggestionUtils.toGetSuggestionResponse(suggestion, materialById);
    }

    @Transactional
    public void updateSuggestion(UUID id, UpdateSuggestionRequest request) {
        log.info("Suggestion Service : updateSuggestion");

        Suggestion existingSuggestion = suggestionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Suggestion not found"));

        existingSuggestion.setHouseNumber(
                request.getHouseNumber() != null ? request.getHouseNumber() : existingSuggestion.getHouseNumber());
        existingSuggestion.setWindDirection(
                request.getWindDirection() != null ? request.getWindDirection()
                        : existingSuggestion.getWindDirection());
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

        log.info("Suggestion Service : updateSuggestion - Save updated suggestion to DB");
        suggestionRepository.save(existingSuggestion);
    }

    public void deleteSuggestion(UUID id) {
        log.info("Suggestion Service : deleteSuggestion");

        Suggestion suggestion = suggestionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Suggestion not found"));

        // Delete all files from S3 bucket
        log.info("Suggestion Service : deleteSuggestion - Delete all files from S3 bucket");
        List<String> floorPlans = suggestion.getFloorplans();
        if (floorPlans != null) {
            for (String floorPlan : floorPlans) {
                log.info("Suggestion Service : deleteSuggestion - Delete floorplan file (" + floorPlan + ") from S3");
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
        if (suggestion.getPdf() != null) {
            s3Service.deleteFile(suggestion.getPdf());
        }

        log.info("Suggestion Service : deleteSuggestion - Delete suggestion from DB");
        suggestionRepository.deleteById(id);
    }

    public GenerateSuggestionResponse generateSuggestion(GenerateSuggestionRequest req) {
        log.info("Suggestion Service : generateSuggestion");

        GenerateSuggestionResponse response = new GenerateSuggestionResponse();
        response.setUserInput(req);
        response.setSuggestions(new SuggestionResponse[0]);

        String style = req.getStyle().trim();
        int landArea = req.getLandArea();
        int floor = req.getFloor();

        /* Fetch all suggestions with EXACT style */
        log.info("Suggestion Service : generateSuggestion - Fetch all suggestions by style from DB");
        List<Suggestion> pool = suggestionRepository.findByStyleIgnoreCase(style);
        if (pool.isEmpty())
            return response;

        // ── Rule 1 ─ exact match ───────────────────────────────────────
        log.info("Suggestion Service : generateSuggestion - 1st Rule (exact Match)");
        List<Suggestion> selected = pool.stream()
                .filter(s -> s.getLandArea() == landArea && s.getFloor() == floor)
                .toList();

        // ── Rule 2 ─ same floor, closest smaller landArea ──────────────
        log.info("Suggestion Service : generateSuggestion - 2nd Rule (same floor, closest smaller landArea)");
        if (selected.isEmpty()) {
            Optional<Suggestion> opt = pool.stream()
                    .filter(s -> s.getFloor() == floor && s.getLandArea() < landArea)
                    .max(Comparator.comparingInt(Suggestion::getLandArea));

            if (opt.isPresent()) {
                selected = List.of(opt.get());
            }
        }

        // ── Rule 3 ─ same landArea, different floor ────────────────────
        log.info("Suggestion Service : generateSuggestion - 3rd Rule (same LandArea, different floor)");
        if (selected.isEmpty()) {
            selected = pool.stream()
                    .filter(s -> s.getLandArea() == landArea && s.getFloor() != floor)
                    .toList();
        }

        // ── Rule 4 ─ smaller landArea, different floor ─────────────────
        log.info("Suggestion Service : generateSuggestion - 4th Rule (smaller landArea, different floor)");
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
            return response;
        }

        /* ── bulk-load materials only once ─────────────────────── */
        log.info(
                "Suggestion Service : generateSuggestion - Collect all material ids from selected suggestions (SuggestionUtils.collectMaterialIds)");
        Set<UUID> matIds = SuggestionUtils.collectMaterialIds(selected);
        Map<UUID, Material> mats = materialRepository.findAllById(matIds)
                .stream()
                .collect(Collectors.toMap(Material::getId, m -> m));

        /* map to Array of Suggestion */
        log.info(
                "Suggestion Service : generateSuggestion - Map selected suggestions to SuggestionResponse (SuggestionUtils.toGetSuggestionResponse)");
        SuggestionResponse[] suggestions = selected.stream()
                .map(s -> SuggestionUtils.toGetSuggestionResponse(s, mats))
                .toArray(SuggestionResponse[]::new);

        response.setSuggestions(suggestions);

        return response;
    }
}
