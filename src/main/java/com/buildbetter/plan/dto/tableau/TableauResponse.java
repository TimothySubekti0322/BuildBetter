package com.buildbetter.plan.dto.tableau;

import java.util.UUID;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Builder
public class TableauResponse {
    private UUID id;
    private int landArea;
    private int buildingArea;
    private String style;
    private int floor;
    private int rooms;
    private String houseImageFront;
    private String houseImageSide;
    private String houseImageBack;
    private String floorPlan1;
    private String floorPlan2;
    private String floorPlan3;
    private String floorPlan4;
    private String designer;
    private String originalBudget;
    private String originalRoof;
    private String originalRoofStructure;
    private String originalPlafon;
    private String originalWallCladding;
    private String originalWallStructure;
    private String originalDoor;
    private String originalShutter;
    private String originalWindowFrame;
    private String originalCoating;
    private String originalBeamColumnStructure;
    private String economicalBudget;
    private String economicalRoof;
    private String economicalRoofStructure;
    private String economicalPlafon;
    private String economicalWallCladding;
    private String economicalWallStructure;
    private String economicalDoor;
    private String economicalShutter;
    private String economicalWindowFrame;
    private String economicalCoating;
    private String economicalBeamColumnStructure;
    private String premiumBudget;
    private String premiumRoof;
    private String premiumRoofStructure;
    private String premiumPlafon;
    private String premiumWallCladding;
    private String premiumWallStructure;
    private String premiumDoor;
    private String premiumShutter;
    private String premiumWindowFrame;
    private String premiumCoating;
    private String premiumBeamColumnStructure;
}
