package com.buildbetter.consult.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.buildbetter.consult.dto.consult.ConsultDateRange;
import com.buildbetter.consult.model.Consult;

@Repository
public interface ConsultRepository extends JpaRepository<Consult, UUID> {
  // Find future Start Time and End Time by Architect ID
  @Query("""
      SELECT new com.buildbetter.consult.dto.consult.ConsultDateRange(
        c.startDate,
        c.endDate
      )
      FROM Consult c
      WHERE c.architectId = :architectId
        AND c.startDate > CURRENT_TIMESTAMP
      """)
  List<ConsultDateRange> findFutureDateRangesByArchitect(UUID architectId);

  List<Consult> findByArchitectIdAndStartDateGreaterThanEqualOrderByStartDate(
      UUID architectId,
      LocalDateTime from);

  List<Consult> findByArchitectIdAndStartDateGreaterThanEqualAndStatusNotOrderByStartDate(
      UUID architectId,
      LocalDateTime from,
      String statusNot);

  List<Consult> findByStatusNot(String status);

  List<Consult> findByStatus(String status);

  List<Consult> findByArchitectId(UUID architectId);
}
