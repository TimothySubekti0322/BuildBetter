package com.buildbetter.consult.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.buildbetter.consult.dto.consult.CreateConsultRequest;
import com.buildbetter.consult.dto.consult.Schedule;
import com.buildbetter.consult.model.Consult;
import com.buildbetter.consult.repository.ConsultRepository;
import com.buildbetter.consult.util.ConsultUtils;
import com.buildbetter.shared.exception.BadRequestException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultService {

    private final ConsultRepository consultRepository;

    public UUID createConsult(CreateConsultRequest request, UUID userId) {
        LocalDateTime now = LocalDateTime.now();
        List<Consult> upcoming = consultRepository
                .findByArchitectIdAndStartDateGreaterThanEqualAndStatusNotOrderByStartDate(request.getArchitectId(),
                        now, "cancelled");

        log.info("Upcoming Consults: {}", upcoming);

        LocalDateTime newStart = request.getStartDate();
        LocalDateTime newEnd = request.getEndDate();

        for (Consult booked : upcoming) {
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

        Consult consult = Consult.builder()
                .architectId(request.getArchitectId())
                .userId(userId)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .type(request.getType())
                .total(request.getTotal())
                .status("waiting-for-payment")
                .build();

        return consultRepository.save(consult).getId();
    }

    public List<Schedule> getArchitectSchedules(UUID architectId) {
        // 1) fetch all future bookings for this architect
        LocalDateTime now = LocalDateTime.now();
        List<Consult> upcoming = consultRepository
                .findByArchitectIdAndStartDateGreaterThanEqualAndStatusNotOrderByStartDate(architectId, now,
                        "cancelled");

        // 2) map into Schedule DTOs
        return ConsultUtils.createUpcomingSchedule(upcoming);
    }

    public List<Consult> getAllConsultsByArchitectId(UUID architectId, String type, String status,
            Boolean includeCancelled) {
        // Start with base query - consults for architect with future start dates
        List<Consult> consults = consultRepository.findByArchitectIdAndStartDateGreaterThanEqualOrderByStartDate(
                architectId, LocalDateTime.now());

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

    public List<Consult> getAllConsults(String type, String status, Boolean includeCancelled) {
        // Get base dataset
        List<Consult> consults;

        // Handle includeCancelled filter at repository level for efficiency
        if (includeCancelled != null && includeCancelled) {
            consults = consultRepository.findAll();
        } else {
            // If includeCancelled is false or null, exclude cancelled consults
            consults = consultRepository.findByStatusNot("cancelled");
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
}
