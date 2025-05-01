package com.buildbetter.plan.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "plans")
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", columnDefinition = "uuid", nullable = false)
    private UUID userId;

    /* ──────────── location info ──────────── */
    @Column(name = "province", nullable = false)
    private String province;
    @Column(name = "city", nullable = false)
    private String city;

    /* ──────────── land details ──────────── */
    @Column(name = "land_form", nullable = false)
    private String landform;

    @Column(name = "land_area", nullable = false)
    private Integer landArea;

    @Column(name = "entrance_direction", nullable = false)
    private String entranceDirection;

    /* ──────────── building spec ─── */
    @Column(name = "style", nullable = false)
    private String style;

    @Column(name = "floor", nullable = false)
    private Integer floor;

    @Column(name = "rooms", nullable = false)
    private Integer rooms;

    /* ──────────── suggestion ──────────── */
    @ManyToOne(fetch = FetchType.LAZY)
    @JdbcTypeCode(SqlTypes.UUID)
    @JoinColumn(name = "suggestion_id", referencedColumnName = "id", nullable = false)
    private Suggestion suggestion;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMPTZ DEFAULT now()")
    private LocalDateTime createdAt;
}
