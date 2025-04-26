package com.buildbetter.plan.constant;

import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

/**
 * Matrix of every {category, subCategory} pair that must appear
 * in the material tree.
 *
 * ┌───────────────┬──────────────────────┐
 * │ category │ subCategory │
 * ├───────────────┼──────────────────────┤
 * │ Atap │ Atap │
 * │ Atap │ Struktur Atap │
 * │ Atap │ Plafon │
 * │ Dinding │ Pelapis Dinding │
 * │ Dinding │ Struktur Dinding │
 * │ Bukaan │ Pintu │
 * │ Bukaan │ Daun Jendela │
 * │ Bukaan │ Frame Jendela │
 * │ Lantai │ Pelapis │
 * │ Balok-Kolom │ Struktur Balok Kolom │
 * └───────────────┴──────────────────────┘
 */

@Getter
public enum HouseMaterial {

    // ─────────────── A T A P ──────────────────────────────
    ROOF("Atap", "Atap"),
    ROOF_STRUCTURE("Atap", "Struktur Atap"),
    PLAFON("Atap", "Plafon"),

    // ─────────────── D I N D I N G ────────────────────────
    WALL_COATING("Dinding", "Pelapis Dinding"),
    WALL_STRUCTURE("Dinding", "Struktur Dinding"),

    // ─────────────── B U K A A N ──────────────────────────
    DOOR("Bukaan", "Pintu"),
    SHUTTER("Bukaan", "Daun Jendela"),
    WINDOW_FRAME("Bukaan", "Frame Jendela"),

    // ─────────────── L A N T A I ──────────────────────────
    FLOOR("Lantai", "Pelapis"),

    // ──────────── B A L O K – K O L O M ───────────────────
    BEAM_COLUMN_STRUCTURE("Balok-Kolom", "Struktur Balok-Kolom");

    private final String category;
    private final String subCategory;

    HouseMaterial(String category, String subCategory) {
        this.category = category;
        this.subCategory = subCategory;
    }

    /** keeps existing JSON behaviour → "Atap", "Pintu", … */
    @JsonValue
    public String json() {
        return subCategory;
    }

}
