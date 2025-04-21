package com.buildbetter.plan.constant;

import com.fasterxml.jackson.annotation.JsonValue;

public enum HouseMaterial {

    ROOF("Atap"),
    ROOF_STRUCTURE("Struktur Atap"),
    PLAFON("Plafon"),
    WALL_COATING("Pelapis Dinding"),
    WALL_STRUCTURE("Struktur Dinding"),
    DOOR("Pintu"),
    SHUTTER("Daun Jendela"),
    WINDOW_FRAME("Frame Jendela"),
    COATING("Pelapis"),
    BEAM_COLUMN_STRUCTURE("Struktur Balok Kolom");

    private final String value;

    HouseMaterial(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

}
