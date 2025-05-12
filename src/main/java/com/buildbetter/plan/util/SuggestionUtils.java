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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.buildbetter.plan.dto.suggestions.SuggestionResponse;
import com.buildbetter.plan.dto.suggestions.generate.GenerateSuggestionRequest;
import com.buildbetter.plan.model.Material;
import com.buildbetter.plan.model.Plan;
import com.buildbetter.plan.model.Suggestion;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class SuggestionUtils {
        public static SuggestionResponse toGetSuggestionResponse(Suggestion s,
                        Map<UUID, Material> materialById) {

                return SuggestionResponse.builder()
                                .id(s.getId())
                                .houseNumber(s.getHouseNumber())
                                .windDirection(s.getWindDirection())
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
                                .pdf(s.getPdf())
                                .houseImageFront(s.getHouseImageFront())
                                .houseImageBack(s.getHouseImageBack())
                                .houseImageSide(s.getHouseImageSide())
                                .materials0(groupByCatAndSub(s.getMaterials0(), materialById))
                                .materials1(groupByCatAndSub(s.getMaterials1(), materialById))
                                .materials2(groupByCatAndSub(s.getMaterials2(), materialById))
                                .build();
        }

        public static Map<String, Map<String, List<Material>>> groupByCatAndSub(List<UUID> ids,
                        Map<UUID, Material> materialById) {

                if (ids == null || ids.isEmpty())
                        return Collections.emptyMap();

                // return ids.stream()
                // .map(materialById::get) // null‑safe → skip unknown ids
                // .filter(Objects::nonNull)
                // .map(SuggestionUtils::toDto) // Corrected: Using class name for static method
                // .collect(Collectors.groupingBy(
                // Material::getCategory, // first level
                // LinkedHashMap::new,
                // Collectors.toMap(Material::getSubCategory,
                // Function.identity(),
                // (a, b) -> a, // keep first if duplicate
                // LinkedHashMap::new)));
                return ids.stream()
                                .map(materialById::get) // null-safe lookup
                                .filter(Objects::nonNull) // skip unknown ids
                                // .map(SuggestionUtils::toDto) // keep if you still need DTO conversion
                                .collect(Collectors.groupingBy( // ── 1st level: category
                                                Material::getCategory,
                                                LinkedHashMap::new,
                                                Collectors.groupingBy( // ── 2nd level: sub-category
                                                                Material::getSubCategory,
                                                                LinkedHashMap::new,
                                                                Collectors.toList() // collect every material into a
                                                                                    // List
                                                )));
        }

        public static Material toDto(Material m) {
                return Material.builder()
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

        // public static Map<String, Map<String, Material>> buildMaterialTree(
        // List<UUID> materialIds,
        // Map<UUID, Material> loaded) {

        // Map<String, Map<String, Material>> tree = new LinkedHashMap<>();

        // // fill every (category, subCategory) with a null placeholder
        // for (HouseMaterial hm : HouseMaterial.values()) {
        // tree.computeIfAbsent(hm.getCategory(), k -> new LinkedHashMap<>())
        // .put(hm.getSubCategory(), null); // <── changed line
        // }

        // if (materialIds != null) {
        // for (UUID id : materialIds) {
        // Material m = loaded.get(id);
        // if (m != null) {
        // tree.get(m.getCategory())
        // .put(m.getSubCategory(), m); // overwrite placeholder
        // }
        // }
        // }

        // return tree;
        // }

        // public static SuggestionResponse toArrayOfSuggestionResponses(
        // Suggestion s,
        // Map<UUID, Material> materialMap) {

        // return SuggestionResponse.builder()
        // .id(s.getId())
        // .houseNumber(s.getHouseNumber())

        // // ── dimensions ─────────────────────────────
        // .landArea(s.getLandArea())
        // .buildingArea(s.getBuildingArea())
        // .style(s.getStyle())
        // .floor(s.getFloor())
        // .rooms(s.getRooms())
        // .buildingHeight(s.getBuildingHeight())
        // .designer(s.getDesigner())

        // // ── budgeting ──────────────────────────────
        // .defaultBudget(s.getDefaultBudget())
        // .budgetMin(s.getBudgetMin())
        // .budgetMax(s.getBudgetMax())

        // // ── design assets ──────────────────────────
        // .floorplans(s.getFloorplans())
        // .object(s.getObject())
        // .pdf(s.getPdf())
        // .houseImageFront(s.getHouseImageFront())
        // .houseImageBack(s.getHouseImageBack())
        // .houseImageSide(s.getHouseImageSide())

        // // ── material maps ──────────────────────────
        // .materials0(buildMaterialTree(s.getMaterials0(), materialMap))
        // .materials1(buildMaterialTree(s.getMaterials1(), materialMap))
        // .materials2(buildMaterialTree(s.getMaterials2(), materialMap))
        // .build();
        // }

        public static GenerateSuggestionRequest planToGenerateSuggestionRequest(Plan plan,
                        SuggestionResponse suggestion) {
                return new GenerateSuggestionRequest(plan.getProvince(), plan.getCity(),
                                plan.getLandform(), plan.getLandArea(), plan.getEntranceDirection(),
                                suggestion.getStyle(), suggestion.getFloor(), suggestion.getRooms());
        }
}
// SuggestionResponse[] suggestions = new SuggestionResponse[s.length];

// for (int i = 0; i < s.length; i++) {

// suggestions[i] = SuggestionResponse.builder()
// .id(s[i].getId())
// .houseNumber(s[i].getHouseNumber())
// .landArea(s[i].getLandArea())
// .buildingArea(s[i].getBuildingArea())
// .style(s[i].getStyle())
// .floor(s[i].getFloor())
// .rooms(s[i].getRooms())
// .buildingHeight(s[i].getBuildingHeight())
// .designer(s[i].getDesigner())
// .defaultBudget(s[i].getDefaultBudget())
// .budgetMin(s[i].getBudgetMin())
// .budgetMax(s[i].getBudgetMax())
// .floorplans(s[i].getFloorplans())
// .object(s[i].getObject())
// .houseImageFront(s[i].getHouseImageFront())
// .houseImageBack(s[i].getHouseImageBack())
// .houseImageSide(s[i].getHouseImageSide())
// .materials0(buildMaterialTree(s[i].getMaterials0(), materialMap))
// .materials1(buildMaterialTree(s[i].getMaterials1(), materialMap))
// .materials2(buildMaterialTree(s[i].getMaterials2(), materialMap))
// .build();
// }

// return suggestions;
// }
