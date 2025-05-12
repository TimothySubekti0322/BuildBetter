package com.buildbetter.plan.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "suggestions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Suggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "house_number")
    private String houseNumber;

    @Column(name = "wind_direction")
    private List<String> windDirection;

    // ── dimensions ───────────────────────────────────────────
    @Column(name = "land_area")
    private int landArea;
    @Column(name = "building_area")
    private int buildingArea;
    private String style;
    private int floor;
    private int rooms;
    @Column(name = "building_height")
    private int buildingHeight;
    private String designer;

    // ── budgeting ────────────────────────────────────────────
    @Column(name = "default_budget")
    private int defaultBudget;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "budget_min", columnDefinition = "integer[]")
    private List<Integer> budgetMin;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "budget_max", columnDefinition = "integer[]")
    private List<Integer> budgetMax;

    // ── design assets ────────────────────────────────────────
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "floorplans", columnDefinition = "text[]")
    private List<String> floorplans;

    @Column(columnDefinition = "text")
    private String object; // 3‑D object URL

    @Column(name = "house_image_front", columnDefinition = "text")
    private String houseImageFront;
    @Column(name = "house_image_back", columnDefinition = "text")
    private String houseImageBack;
    @Column(name = "house_image_side", columnDefinition = "text")
    private String houseImageSide;

    @Column(name = "pdf", columnDefinition = "text")
    private String pdf; // PDF URL

    // ── material IDs ─────────────────────────────────────────
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "materials_0", columnDefinition = "uuid[]")
    private List<UUID> materials0;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "materials_1", columnDefinition = "uuid[]")
    private List<UUID> materials1;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "materials_2", columnDefinition = "uuid[]")
    private List<UUID> materials2;

    // ── timestamp ───────────────────────────────────────
    /* ───────── timestamps ─────────── */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false, columnDefinition = "TIMESTAMPTZ DEFAULT now()")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMPTZ DEFAULT now()")
    private LocalDateTime updatedAt;
}
