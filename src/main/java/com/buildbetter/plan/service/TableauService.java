package com.buildbetter.plan.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.buildbetter.plan.constant.HouseMaterial;
import com.buildbetter.plan.dto.tableau.TableauResponse;
import com.buildbetter.plan.model.Material;
import com.buildbetter.plan.model.Suggestion;
import com.buildbetter.plan.repository.MaterialRepository;
import com.buildbetter.plan.repository.SuggestionRepository;
import com.buildbetter.plan.util.TableauUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TableauService {

    private final SuggestionRepository suggestionRepository;
    private final MaterialRepository materialRepository; // ⬅ add this

    /**
     * Fetch every suggestion and project it to a Tableau‑friendly DTO.
     */
    public TableauResponse[] getTableauData() {
        log.info("Service : Get Tableau Data");

        List<Suggestion> suggestions = suggestionRepository.findAll();

        // Map ➜ DTO and return as array
        return suggestions.stream()
                .map(this::toResponse)
                .toArray(TableauResponse[]::new);
    }

    /* --------------------------------------------------------------------- */

    /**
     * Convert one Suggestion entity into its API shape.
     */
    private TableauResponse toResponse(Suggestion s) {

        // 1) Floor‑plans: keep max four, fill nulls if list shorter
        String fp1 = null, fp2 = null, fp3 = null, fp4 = null;
        if (s.getFloorplans() != null && !s.getFloorplans().isEmpty()) {
            List<String> fps = s.getFloorplans();
            if (fps.size() > 0)
                fp1 = fps.get(0);
            if (fps.size() > 1)
                fp2 = fps.get(1);
            if (fps.size() > 2)
                fp3 = fps.get(2);
            if (fps.size() > 3)
                fp4 = fps.get(3);
        }

        // 2) Resolve ORIGINAL (materials_1) UUIDs to material names by category
        Map<String, String> economicalMaterials = resolveOriginalMaterials(s.getMaterials0());
        Map<String, String> originalMaterials = resolveOriginalMaterials(s.getMaterials1());
        Map<String, String> premiumMaterials = resolveOriginalMaterials(s.getMaterials2());

        List<String> budgetRange = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            String min = TableauUtils.safeGet(s.getBudgetMin(), i);
            String max = TableauUtils.safeGet(s.getBudgetMax(), i);
            budgetRange.add(min + " - " + max);
            log.info("i = " + i);
        }

        /* 3) Build the DTO -------------------------------------------------- */
        return TableauResponse.builder()
                .id(s.getId())
                .landArea(s.getLandArea())
                .buildingArea(s.getBuildingArea())
                .style(s.getStyle())
                .floor(s.getFloor())
                .rooms(s.getRooms())
                .houseImageFront(s.getHouseImageFront())
                .houseImageSide(s.getHouseImageSide())
                .houseImageBack(s.getHouseImageBack())
                .floorPlan1(fp1)
                .floorPlan2(fp2)
                .floorPlan3(fp3)
                .floorPlan4(fp4)
                .designer(s.getDesigner())
                .originalBudget(budgetRange.get(0))
                .originalRoof(originalMaterials.get(HouseMaterial.ROOF.getSubCategory()))
                .originalRoofStructure(originalMaterials.get(HouseMaterial.ROOF_STRUCTURE.getSubCategory()))
                .originalPlafon(originalMaterials.get(HouseMaterial.PLAFON.getSubCategory()))
                .originalWallCladding(originalMaterials.get(HouseMaterial.WALL_COATING.getSubCategory()))
                .originalWallStructure(originalMaterials.get(HouseMaterial.WALL_STRUCTURE.getSubCategory()))
                .originalDoor(originalMaterials.get(HouseMaterial.DOOR.getSubCategory()))
                .originalShutter(originalMaterials.get(HouseMaterial.SHUTTER.getSubCategory()))
                .originalWindowFrame(originalMaterials.get(HouseMaterial.WINDOW_FRAME.getSubCategory()))
                .originalCoating(originalMaterials.get(HouseMaterial.FLOOR.getSubCategory()))
                .originalBeamColumnStructure(
                        originalMaterials.get(HouseMaterial.BEAM_COLUMN_STRUCTURE.getSubCategory()))
                .economicalBudget(budgetRange.get(1))
                .economicalRoof(economicalMaterials.get(HouseMaterial.ROOF.getSubCategory()))
                .economicalRoofStructure(economicalMaterials.get(HouseMaterial.ROOF_STRUCTURE.getSubCategory()))
                .economicalPlafon(economicalMaterials.get(HouseMaterial.PLAFON.getSubCategory()))
                .economicalWallCladding(economicalMaterials.get(HouseMaterial.WALL_COATING.getSubCategory()))
                .economicalWallStructure(economicalMaterials.get(HouseMaterial.WALL_STRUCTURE.getSubCategory()))
                .economicalDoor(economicalMaterials.get(HouseMaterial.DOOR.getSubCategory()))
                .economicalShutter(economicalMaterials.get(HouseMaterial.SHUTTER.getSubCategory()))
                .economicalWindowFrame(economicalMaterials.get(HouseMaterial.WINDOW_FRAME.getSubCategory()))
                .economicalCoating(economicalMaterials.get(HouseMaterial.FLOOR.getSubCategory()))
                .economicalBeamColumnStructure(
                        economicalMaterials.get(HouseMaterial.BEAM_COLUMN_STRUCTURE.getSubCategory()))
                .premiumBudget(budgetRange.get(2))
                .premiumRoof(premiumMaterials.get(HouseMaterial.ROOF.getSubCategory()))
                .premiumRoofStructure(premiumMaterials.get(HouseMaterial.ROOF_STRUCTURE.getSubCategory()))
                .premiumPlafon(premiumMaterials.get(HouseMaterial.PLAFON.getSubCategory()))
                .premiumWallCladding(premiumMaterials.get(HouseMaterial.WALL_COATING.getSubCategory()))
                .premiumWallStructure(premiumMaterials.get(HouseMaterial.WALL_STRUCTURE.getSubCategory()))
                .premiumDoor(premiumMaterials.get(HouseMaterial.DOOR.getSubCategory()))
                .premiumShutter(premiumMaterials.get(HouseMaterial.SHUTTER.getSubCategory()))
                .premiumWindowFrame(premiumMaterials.get(HouseMaterial.WINDOW_FRAME.getSubCategory()))
                .premiumCoating(premiumMaterials.get(HouseMaterial.FLOOR.getSubCategory()))
                .premiumBeamColumnStructure(premiumMaterials.get(HouseMaterial.BEAM_COLUMN_STRUCTURE.getSubCategory()))
                .build();
    }

    /**
     * Helper: take the list of ORIGINAL material UUIDs, look them up once,
     * and return a map “category → material‑name”.
     */
    private Map<String, String> resolveOriginalMaterials(List<UUID> materialIds) {
        if (materialIds == null || materialIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Material> materials = materialRepository.findAllById(materialIds);

        // Assuming `Material.category` already holds strings like “roof”, “door”, etc.
        return materials.stream()
                .collect(Collectors.toMap(
                        Material::getSubCategory, // key = category
                        Material::getName, // value = material name
                        (first, second) -> first // on duplicate category keep 1st
                ));
    }
}
