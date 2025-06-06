package com.buildbetter.consultation.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
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

  List<Consultation> findByStatus(String status);

  List<Consultation> findByStatusNot(String status);

  List<Consultation> findByStatusNotOrderByStartDate(String status);

  List<Consultation> findByStatusNotAndStartDateGreaterThanEqualOrderByStartDate(
      String statusNot,
      LocalDateTime from);

  List<Consultation> findByArchitectId(UUID architectId);

  List<Consultation> findByArchitectIdOrderByStartDate(UUID architectId);

  List<Consultation> findByStartDateGreaterThanEqualOrderByStartDate(LocalDateTime from);

  List<Consultation> findByUserIdAndStartDateGreaterThanEqualOrderByStartDate(UUID userId, LocalDateTime from);

  List<Consultation> findByUserIdOrderByStartDate(UUID userId);

  List<Consultation> findByUserIdAndStatusIn(UUID userId, Collection<String> statuses);

  List<Consultation> findAllByStatusIn(Collection<String> statuses);

  Consultation findByArchitectIdAndUserIdAndStatusInAndEndDateAfter(UUID architectId,
      UUID userId,
      Collection<String> statuses,
      LocalDateTime now);

  // boolean existsByArchitectIdAndUserIdAndStatusInAndEndDateAfter(
  // UUID architectId,
  // UUID userId,
  // Collection<String> statuses,
  // LocalDateTime now);

  @Query("""
        SELECT DISTINCT c.architectId
        FROM Consultation c
        WHERE c.userId   = :userId
          AND c.status  IN :statuses
      """)
  List<UUID> findDistinctArchitectIdByUserIdAndStatusIn(
      UUID userId,
      Collection<String> statuses);

  Optional<Consultation> findByRoomIdAndEndDate(UUID roomId, LocalDateTime endDate);

}
