package com.buildbetter.plan.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.buildbetter.plan.dto.materials.MaterialResponse;
import com.buildbetter.plan.dto.suggestions.SuggestionResponse;
import com.buildbetter.plan.model.Material;
import com.buildbetter.plan.model.Suggestion;

public class SuggestionUtils {
    public static SuggestionResponse toGetSuggestionResponse(Suggestion s,
            Map<UUID, Material> materialById) {

        return SuggestionResponse.builder()
                .id(s.getId())
                .houseNumber(s.getHouseNumber())
                .landArea(s.getLandArea())
                .buildingArea(s.getBuildingArea())
                .style(s.getStyle())
                .floor(s.getFloor())
                .rooms(s.getRooms())
                .buildingHeight(s.getBuildingHeight())
                .designer(s.getDesigner())
                .defaultBudget(s.getDefaultBudget())
                .budgetMin(s.getBudgetMin())
                .budgetMax(s.getBudgetMax())
                .floorplans(s.getFloorplans())
                .object(s.getObject())
                .houseImageFront(s.getHouseImageFront())
                .houseImageBack(s.getHouseImageBack())
                .houseImageSide(s.getHouseImageSide())
                .materials0(groupByCatAndSub(s.getMaterials0(), materialById))
                .materials1(groupByCatAndSub(s.getMaterials1(), materialById))
                .materials2(groupByCatAndSub(s.getMaterials2(), materialById))
                .build();
    }

    public static Map<String, Map<String, MaterialResponse>> groupByCatAndSub(List<UUID> ids,
            Map<UUID, Material> materialById) {

        if (ids == null || ids.isEmpty())
            return Collections.emptyMap();

        return ids.stream()
                .map(materialById::get) // null‑safe → skip unknown ids
                .filter(Objects::nonNull)
                .map(SuggestionUtils::toDto) // Corrected: Using class name for static method
                .collect(Collectors.groupingBy(
                        MaterialResponse::getCategory, // first level
                        LinkedHashMap::new,
                        Collectors.toMap(MaterialResponse::getSubCategory,
                                Function.identity(),
                                (a, b) -> a, // keep first if duplicate
                                LinkedHashMap::new)));
    }

    public static MaterialResponse toDto(Material m) {
        return MaterialResponse.builder()
                .id(m.getId())
                .name(m.getName())
                .category(m.getCategory())
                .subCategory(m.getSubCategory())
                .image(m.getImage())
                .build();
    }
}