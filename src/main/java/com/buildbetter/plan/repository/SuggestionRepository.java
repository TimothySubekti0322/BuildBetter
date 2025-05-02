package com.buildbetter.plan.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.buildbetter.plan.model.Suggestion;

@Repository
public interface SuggestionRepository extends JpaRepository<Suggestion, UUID> {

        /*
         * ────────────────────────────────────────────────────────────────
         * NOTE:
         * All queries are native because JPQL/HQL does not support the
         * `ANY(array_column)` syntax.
         * Column names use snake_case to match the table definition.
         * ───────────────────────────────────────────────────────────────
         */

        /* budget_min integer[] ────────────────────────────────────────── */
        @Query(value = "SELECT * FROM suggestions s " +
                        "WHERE :budgetValue = ANY(s.budget_min)", nativeQuery = true)
        List<Suggestion> findByBudgetMinContains(@Param("budgetValue") Integer budgetValue);

        /* material_0 uuid[] ──────────────────────────────────────────── */
        @Query(value = "SELECT * FROM suggestions s " +
                        "WHERE :materialId = ANY(s.material_0)", nativeQuery = true)
        List<Suggestion> findByMaterial0Contains(@Param("materialId") UUID materialId);

        /* material_1 uuid[] ──────────────────────────────────────────── */
        @Query(value = "SELECT * FROM suggestions s " +
                        "WHERE :materialId = ANY(s.material_1)", nativeQuery = true)
        List<Suggestion> findByMaterial1Contains(@Param("materialId") UUID materialId);

        /* material_2 uuid[] ──────────────────────────────────────────── */
        @Query(value = "SELECT * FROM suggestions s " +
                        "WHERE :materialId = ANY(s.material_2)", nativeQuery = true)
        List<Suggestion> findByMaterial2Contains(@Param("materialId") UUID materialId);

        /* any of the three material arrays ───────────────────────────── */
        @Query(value = "SELECT * FROM suggestions s " +
                        "WHERE :materialId = ANY(s.material_0) " +
                        "   OR :materialId = ANY(s.material_1) " +
                        "   OR :materialId = ANY(s.material_2)", nativeQuery = true)
        List<Suggestion> findByMaterialIdInAnyArray(@Param("materialId") UUID materialId);

        /* floorplans text[] ──────────────────────────────────────────── */
        @Query(value = "SELECT * FROM suggestions s " +
                        "WHERE :floorplan = ANY(s.floorplans)", nativeQuery = true)
        List<Suggestion> findByFloorplanContains(@Param("floorplan") String floorplan);

        List<Suggestion> findByStyleIgnoreCase(String style);

        @Query(value = "SELECT * FROM suggestions s " +
                        "ORDER BY s.created_at DESC", nativeQuery = true)
        List<Suggestion> findAllSortedByCreatedAt();

}
