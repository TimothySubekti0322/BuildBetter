package com.buildbetter.plan.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.buildbetter.plan.model.Plan;
import com.buildbetter.plan.model.Suggestion;

@Repository
public interface PlanRepository extends JpaRepository<Plan, UUID> {

    /** All plans owned by a single user (ordered newest-first). */
    List<Plan> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /** All plans in a given city / province. */
    List<Plan> findByProvinceAndCity(String province, String city);

    @Query("""
            select p.id, p.createdAt
              from Plan p
             where p.userId = :userId
             order by p.createdAt desc
            """)
    List<Object[]> findIdsAndDatesByUserId(UUID userId);

    @Query("""
            select s
            from Plan p
            join p.suggestion s
            where p.id = :planId
            """)
    Optional<Suggestion> findSuggestionByPlanId(UUID planId);

}
