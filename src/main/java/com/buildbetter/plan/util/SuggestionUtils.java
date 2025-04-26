package com.buildbetter.plan.util;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.buildbetter.plan.constant.HouseMaterial;
import com.buildbetter.plan.dto.materials.MaterialResponse;
import com.buildbetter.plan.dto.suggestions.GenerateSuggestionResponse;
import com.buildbetter.plan.dto.suggestions.SuggestionResponse;
import com.buildbetter.plan.model.Material;
import com.buildbetter.plan.model.Suggestion;

import lombok.NoArgsConstructor;

@NoArgsConstructor
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

    /** Collect every UUID in the three material arrays of one suggestion. */
    public static Set<UUID> collectMaterialIds(Suggestion s) {
        return Stream.of(
                Optional.ofNullable(s.getMaterials0()).orElse(List.of()),
                Optional.ofNullable(s.getMaterials1()).orElse(List.of()),
                Optional.ofNullable(s.getMaterials2()).orElse(List.of()))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    /** Collect IDs for many suggestions at once. */
    public static Set<UUID> collectMaterialIds(Collection<Suggestion> suggestions) {
        return suggestions.stream()
                .flatMap(s -> collectMaterialIds(s).stream())
                .collect(Collectors.toSet());
    }

    public static Map<String, Map<String, Material>> buildMaterialTree(
            List<UUID> materialIds,
            Map<UUID, Material> loaded) {

        Map<String, Map<String, Material>> tree = new LinkedHashMap<>();

        // fill every (category, subCategory) with a null placeholder
        for (HouseMaterial hm : HouseMaterial.values()) {
            tree.computeIfAbsent(hm.getCategory(), k -> new LinkedHashMap<>())
                    .put(hm.getSubCategory(), null); // <── changed line
        }

        if (materialIds != null) {
            for (UUID id : materialIds) {
                Material m = loaded.get(id);
                if (m != null) {
                    tree.get(m.getCategory())
                            .put(m.getSubCategory(), m); // overwrite placeholder
                }
            }
        }

        return tree;
    }


    public static GenerateSuggestionResponse toGenerateSuggestionResponseDto(
            Suggestion s,
            Map<UUID, Material> materialMap) {

        return GenerateSuggestionResponse.builder()
                .id(s.getId())
                .houseNumber(s.getHouseNumber())

                // ── dimensions ─────────────────────────────
                .landArea(s.getLandArea())
                .buildingArea(s.getBuildingArea())
                .style(s.getStyle())
                .floor(s.getFloor())
                .rooms(s.getRooms())
                .buildingHeight(s.getBuildingHeight())
                .designer(s.getDesigner())

                // ── budgeting ──────────────────────────────
                .defaultBudget(s.getDefaultBudget())
                .budgetMin(s.getBudgetMin())
                .budgetMax(s.getBudgetMax())

                // ── design assets ──────────────────────────
                .floorplans(s.getFloorplans())
                .object(s.getObject())
                .houseImageFront(s.getHouseImageFront())
                .houseImageBack(s.getHouseImageBack())
                .houseImageSide(s.getHouseImageSide())

                // ── material maps ──────────────────────────
                .materials0(buildMaterialTree(s.getMaterials0(), materialMap))
                .materials1(buildMaterialTree(s.getMaterials1(), materialMap))
                .materials2(buildMaterialTree(s.getMaterials2(), materialMap))
                .build();
    }

    // public static GenerateSuggestionResponse
    // toGenerateSuggestionResponseDto(Suggestion s,
    // Map<UUID, Material> materialMap) {
    // return GenerateSuggestionResponse.builder()
    // .id(s.getId())
    // .houseNumber(s.getHouseNumber())
    // .landArea(s.getLandArea())
    // .buildingArea(s.getBuildingArea())
    // .style(s.getStyle())
    // .floor(s.getFloor())
    // .rooms(s.getRooms())
    // .buildingHeight(s.getBuildingHeight())
    // .designer(s.getDesigner())
    // .defaultBudget(s.getDefaultBudget())
    // .budgetMin(s.getBudgetMin())
    // .budgetMax(s.getBudgetMax())
    // .floorplans(s.getFloorplans())
    // .object(s.getObject())
    // .houseImageFront(s.getHouseImageFront())
    // .houseImageBack(s.getHouseImageBack())
    // .houseImageSide(s.getHouseImageSide())
    // .materials0(resolveMaterials(s.getMaterials0(), materialMap))
    // .materials1(resolveMaterials(s.getMaterials1(), materialMap))
    // .materials2(resolveMaterials(s.getMaterials2(), materialMap))
    // .build();
    // }

    // public static Map<String, Map<String, MaterialResponse>>
    // resolveMaterials(List<UUID> ids,
    // Map<UUID, Material> materialMap) {
    // if (ids == null || ids.isEmpty())
    // return Collections.emptyMap();

    // Map<String, Map<String, MaterialResponse>> result = new HashMap<>();

    // for (UUID id : ids) {
    // Material m = materialMap.get(id);
    // if (m == null)
    // continue;

    // String cat = m.getCategory();
    // String sub = m.getSubCategory();

    // result.computeIfAbsent(cat, k -> new HashMap<>());
    // if (sub != null && !sub.isBlank()) {
    // result.get(cat).put(sub, toMaterialResponseDto(m));
    // }
    // }

    // return result;
    // }

    // public static MaterialResponse toMaterialResponseDto(Material m) {
    // return MaterialResponse.builder()
    // .id(m.getId())
    // .name(m.getName())
    // .category(m.getCategory())
    // .subCategory(m.getSubCategory())
    // .image(m.getImage())
    // .build();
    // }
}