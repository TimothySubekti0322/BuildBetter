package com.buildbetter.plan.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.buildbetter.plan.model.Plan;
import com.buildbetter.plan.model.Suggestion;

@Repository
public interface PlanRepository extends JpaRepository<Plan, UUID> {

    /* ───────────── derived queries (no custom SQL) ───────────── */

    List<Plan> findByUserId(UUID userId);

    List<Plan> findBySuggestion(Suggestion suggestion);

    List<Plan> findBySuggestionId(UUID suggestionId);

    List<Plan> findByProvinceAndCityAndDistrict(String province, String city, String district);

    List<Plan> findByStyle(String style);

    List<Plan> findByFloor(Integer floor);

    List<Plan> findByRooms(Integer rooms);

    List<Plan> findByLengthAndWidth(Integer length, Integer width);

    List<Plan> findByFloodProneTrue();

    List<Plan> findByLandform(String landform);

    List<Plan> findByEntranceDirection(String entranceDirection);

    /* land‑area filter (JPQL is fine here) */
    @Query("SELECT p FROM Plan p WHERE p.length * p.width >= :minArea")
    List<Plan> findByLandAreaGreaterThanEqual(@Param("minArea") Integer minArea);

    /* ───────────── native query for array lookup ───────────── */

    /**
     * Return all plans whose linked Suggestion contains the given material UUID
     * in <code>material_0</code>, <code>material_1</code> or
     * <code>material_2</code>.
     *
     * Uses PostgreSQL’s <code>ANY(uuid[])</code> operator, so we must mark it
     * <code>nativeQuery = true</code>.
     */
    @Query(value = """
            SELECT p.*
            FROM plans p
            WHERE p.suggestion_id IN (
                SELECT s.id
                FROM suggestions s
                WHERE :materialId = ANY(s.material_0)
                   OR :materialId = ANY(s.material_1)
                   OR :materialId = ANY(s.material_2)
            )
            """, nativeQuery = true)
    List<Plan> findByMaterialId(@Param("materialId") UUID materialId);

    /* ───────────── misc helpers ───────────── */

    long countByUserId(UUID userId);

    boolean existsByUserId(UUID userId);
}
