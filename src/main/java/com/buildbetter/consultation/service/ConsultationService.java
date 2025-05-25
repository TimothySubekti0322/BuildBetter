package com.buildbetter.consultation.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.buildbetter.consultation.dto.consultation.CreateConsultationRequest;
import com.buildbetter.consultation.dto.consultation.Schedule;
import com.buildbetter.consultation.model.Consultation;
import com.buildbetter.consultation.repository.ConsultationRepository;
import com.buildbetter.consultation.util.ConsultationUtils;
import com.buildbetter.shared.exception.BadRequestException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultationService {

    private final ConsultationRepository consultationRepository;

    public UUID createConsult(CreateConsultationRequest request, UUID userId) {
        LocalDateTime now = LocalDateTime.now();
        List<Consultation> upcoming = consultationRepository
                .findByArchitectIdAndStartDateGreaterThanEqualAndStatusNotOrderByStartDate(request.getArchitectId(),
                        now, "cancelled");

        log.info("Upcoming Consults: {}", upcoming);

        LocalDateTime newStart = request.getStartDate();
        LocalDateTime newEnd = request.getEndDate();

        for (Consultation booked : upcoming) {
            if ("offline".equalsIgnoreCase(booked.getType())) {
                // build a 1-hour buffer around the offline slot
                LocalDateTime bufferStart = booked.getStartDate().minusHours(1);
                LocalDateTime bufferEnd = booked.getEndDate().plusHours(1);

                // if new slot overlaps that expanded window → reject
                if (newStart.isBefore(bufferEnd) && bufferStart.isBefore(newEnd)) {
                    throw new BadRequestException(String.format(
                            "Requested slot [%s – %s] is too close to offline booking [%s – %s]. " +
                                    "Please choose a time before %s or after %s.",
                            newStart, newEnd,
                            booked.getStartDate(), booked.getEndDate(),
                            bufferStart, bufferEnd));
                }
            } else {
                // normal overlap check
                if (booked.getStartDate().isBefore(newEnd) &&
                        newStart.isBefore(booked.getEndDate())) {
                    throw new BadRequestException(String.format(
                            "Requested slot [%s – %s] overlaps with existing booking [%s – %s].",
                            newStart, newEnd,
                            booked.getStartDate(), booked.getEndDate()));
                }
            }
        }

        log.info("No overlaps detected. Proceeding to create consult.");

        Consultation consult = Consultation.builder()
                .architectId(request.getArchitectId())
                .userId(userId)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .type(request.getType())
                .total(request.getTotal())
                .status("waiting-for-payment")
                .build();

        return consultationRepository.save(consult).getId();
    }

    public List<Schedule> getArchitectSchedules(UUID architectId) {
        // 1) fetch all future bookings for this architect
        LocalDateTime now = LocalDateTime.now();
        List<Consultation> upcoming = consultationRepository
                .findByArchitectIdAndStartDateGreaterThanEqualAndStatusNotOrderByStartDate(architectId, now,
                        "cancelled");

        // 2) map into Schedule DTOs
        return ConsultationUtils.createUpcomingSchedule(upcoming);
    }

    public List<Consultation> getAllConsultsByArchitectId(UUID architectId, String type, String status,
            Boolean includeCancelled, Boolean upcoming) {

        List<Consultation> consults;

        if (upcoming != null && upcoming) {
            consults = consultationRepository.findByArchitectIdAndStartDateGreaterThanEqualOrderByStartDate(
                    architectId, LocalDateTime.now());
        } else {
            consults = consultationRepository.findByArchitectIdOrderByStartDate(architectId);
        }

        // Apply filters using streams
        return consults.stream()
                .filter(consult -> {
                    // Filter by type if specified
                    if (type != null && !type.trim().isEmpty()) {
                        return type.equalsIgnoreCase(consult.getType());
                    }
                    return true;
                })
                .filter(consult -> {
                    // Filter by status if specified
                    if (status != null && !status.trim().isEmpty()) {
                        return status.equalsIgnoreCase(consult.getStatus());
                    }
                    return true;
                })
                .filter(consult -> {
                    // Filter out cancelled consults unless explicitly included
                    if (includeCancelled != null && !includeCancelled) {
                        return !"CANCELLED".equalsIgnoreCase(consult.getStatus());
                    }
                    // If includeCancelled is null or true, include all statuses
                    return true;
                })
                .collect(Collectors.toList());
    }

    public List<Consultation> getAllConsults(String type, String status, Boolean includeCancelled, Boolean upcoming) {
        // Get base dataset
        List<Consultation> consults;

        // Handle includeCancelled filter at repository level for efficiency
        if (includeCancelled != null && includeCancelled) {
            if (upcoming != null && upcoming) {
                consults = consultationRepository
                        .findByStartDateGreaterThanEqualOrderByStartDate(LocalDateTime.now());
            } else {
                consults = consultationRepository.findAll(
                        Sort.by(Sort.Direction.ASC, "startDate"));
            }
        } else {
            if (upcoming != null && upcoming) {
                consults = consultationRepository
                        .findByStatusNotAndStartDateGreaterThanEqualOrderByStartDate("cancelled", LocalDateTime.now());
            } else {
                consults = consultationRepository.findByStatusNotOrderByStartDate("cancelled");
            }
        }

        // Apply additional filters using streams
        return consults.stream()
                .filter(consult -> {
                    // Filter by type if specified
                    if (type != null && !type.trim().isEmpty()) {
                        return type.equalsIgnoreCase(consult.getType());
                    }
                    return true;
                })
                .filter(consult -> {
                    // Filter by status if specified
                    if (status != null && !status.trim().isEmpty()) {
                        return status.equalsIgnoreCase(consult.getStatus());
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    public Consultation getConsultById(UUID consultId) {
        return consultationRepository.findById(consultId)
                .orElseThrow(() -> new BadRequestException("Consultation not found"));
    }

    public List<Consultation> getUserConsultations(UUID userId, String type, String status,
            Boolean includeCancelled, Boolean upcoming) {
        // Get base dataset
        List<Consultation> consults;

        if (upcoming != null && upcoming) {
            consults = consultationRepository
                    .findByUserIdAndStartDateGreaterThanEqualOrderByStartDate(userId, LocalDateTime.now());
        } else {
            consults = consultationRepository.findByUserIdOrderByStartDate(userId);
        }

        // Apply filters using streams
        return consults.stream()
                .filter(consult -> {
                    // Filter by type if specified
                    if (type != null && !type.trim().isEmpty()) {
                        return type.equalsIgnoreCase(consult.getType());
                    }
                    return true;
                })
                .filter(consult -> {
                    // Filter by status if specified
                    if (status != null && !status.trim().isEmpty()) {
                        return status.equalsIgnoreCase(consult.getStatus());
                    }
                    return true;
                })
                .filter(consult -> {
                    // Filter out cancelled consults unless explicitly included
                    if (includeCancelled != null && !includeCancelled) {
                        return !"CANCELLED".equalsIgnoreCase(consult.getStatus());
                    }
                    // If includeCancelled is null or true, include all statuses
                    return true;
                })
                .collect(Collectors.toList());
    }
}