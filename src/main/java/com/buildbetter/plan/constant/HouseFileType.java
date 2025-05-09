package com.buildbetter.plan.constant;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonValue;

public enum HouseFileType {

    HOUSE_IMAGE_FRONT("house_image_front"),
    HOUSE_IMAGE_BACK("house_image_back"),
    HOUSE_IMAGE_SIDE("house_image_side"),
    HOUSE_OBJECT("house_object"),
    PDF("pdf");

    private final String value;

    HouseFileType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static boolean isValid(String candidate) {
        return Arrays.stream(values())
                .anyMatch(t -> t.value.equals(candidate));
    }

    // Method to get HouseFileType from its name (case-sensitive)
    public static HouseFileType fromName(String name) {
        try {
            return valueOf(name);
        } catch (IllegalArgumentException e) {
            return null; // Or throw an exception if you prefer
        }
    }

    // Method to get HouseFileType from its JsonValue (case-sensitive)
    public static HouseFileType fromValue(String value) {
        if (value == null || value.isEmpty()) {
            return null; // Or throw an IllegalArgumentException
        }
        return Arrays.stream(values())
                .filter(type -> type.value.equals(value))
                .findFirst()
                .orElse(null); // Or throw an IllegalArgumentException if not found
    }

    // Method to get HouseFileType from its JsonValue (case-insensitive)
    public static HouseFileType fromValueIgnoreCase(String value) {
        if (value == null || value.isEmpty()) {
            return null; // Or throw an IllegalArgumentException
        }
        return Arrays.stream(values())
                .filter(type -> type.value.equalsIgnoreCase(value))
                .findFirst()
                .orElse(null); // Or throw an IllegalArgumentException if not found
    }
}
