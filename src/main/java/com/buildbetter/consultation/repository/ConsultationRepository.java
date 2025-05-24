package com.buildbetter.consultation.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.buildbetter.consultation.dto.consultation.ConsultationDateRange;
import com.buildbetter.consultation.model.Consultation;

@Repository
public interface ConsultationRepository extends JpaRepository<Consultation, UUID> {
  // Find future Start Time and End Time by Architect ID
  @Query("""
      SELECT new com.buildbetter.consultation.dto.consultation.ConsultationDateRange(
        c.startDate,
        c.endDate
      )
      FROM Consultation c
      WHERE c.architectId = :architectId
        AND c.startDate > CURRENT_TIMESTAMP
      """)
  List<ConsultationDateRange> findFutureDateRangesByArchitect(UUID architectId);

  List<Consultation> findByArchitectIdAndStartDateGreaterThanEqualOrderByStartDate(
      UUID architectId,
      LocalDateTime from);

  List<Consultation> findByArchitectIdAndStartDateGreaterThanEqualAndStatusNotOrderByStartDate(
      UUID architectId,
      LocalDateTime from,
      String statusNot);

  List<Consultation> findByStatusNot(String status);

  List<Consultation> findByStatusNotOrderByStartDate(String status);

  List<Consultation> findByStatusNotAndStartDateGreaterThanEqualOrderByStartDate(
      String statusNot,
      LocalDateTime from);

  List<Consultation> findByStatus(String status);

  List<Consultation> findByArchitectId(UUID architectId);

  List<Consultation> findByArchitectIdOrderByStartDate(UUID architectId);

  List<Consultation> findByStartDateGreaterThanEqualOrderByStartDate(LocalDateTime from);
}
