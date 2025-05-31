package com.buildbetter.consultation.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.buildbetter.consultation.constant.CancellationReason;
import com.buildbetter.consultation.constant.ConsultationStatus;
import com.buildbetter.consultation.dto.consultation.CreateConsultationRequest;
import com.buildbetter.consultation.dto.consultation.GetConsultationResponse;
import com.buildbetter.consultation.dto.consultation.RejectConsultationRequest;
import com.buildbetter.consultation.dto.consultation.Schedule;
import com.buildbetter.consultation.dto.consultation.UpdateConsultationRequest;
import com.buildbetter.consultation.dto.room.CreateRoomRequest;
import com.buildbetter.consultation.model.Architect;
import com.buildbetter.consultation.model.Consultation;
import com.buildbetter.consultation.repository.ArchitectRepository;
import com.buildbetter.consultation.repository.ConsultationRepository;
import com.buildbetter.consultation.util.ConsultationUtils;
import com.buildbetter.consultation.websocket.confirmation.service.ConfirmationService;
import com.buildbetter.shared.exception.BadRequestException;
import com.buildbetter.user.UserAPI;
import com.buildbetter.user.dto.user.GetUserNameAndCity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final ArchitectRepository architectRepository;
    private final ConfirmationService confirmationService;
    private final RoomService roomService;
    private final UserAPI userApi;

    public UUID createConsult(CreateConsultationRequest request, UUID userId) {
        log.info(
                "Consultation Service : createConsult - Creating consultation for user: {}, architect: {}, type: {}, start: {}, end: {}",
                userId, request.getArchitectId(), request.getType(), request.getStartDate(), request.getEndDate());

        LocalDateTime now = LocalDateTime.now();

        log.info(
                "Consultation Service : Checking if request dates are in the future and start date is before end date: {}, {}",
                request.getStartDate(), request.getEndDate());
        // Validate that start and end dates are in the future
        if (request.getStartDate().isBefore(now) || request.getEndDate().isBefore(now)) {
            throw new BadRequestException("Start and end dates must be in the future.");
        }

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BadRequestException("Start date cannot be after end date.");
        }

        List<String> active = List.of(
                ConsultationStatus.SCHEDULED.getStatus(),
                ConsultationStatus.IN_PROGRESS.getStatus());

        // 1) Fast-fail if *this* user already has one consultation with this architect
        log.info(
                "Consultation Service : createConsult - Checking for active consultations with architect: {}, user: {}, active statuses: {}, current time: {}",
                request.getArchitectId(), userId, active, now);
        if (consultationRepository.existsByArchitectIdAndUserIdAndStatusInAndEndDateAfter(
                request.getArchitectId(),
                userId,
                active,
                now)) {
            throw new BadRequestException(
                    "You already have an active (scheduled or in-progress) booking with this architect.");
        }

        List<Consultation> upcoming = consultationRepository
                .findByArchitectIdAndStartDateGreaterThanEqualAndStatusNotOrderByStartDate(request.getArchitectId(),
                        now, "cancelled");

        log.info("Consultation Service : createConsult - Upcoming Consults: {}", upcoming);

        // Check wheter there is upcoming booking between user and architect
        log.info(
                "Consultation Service : createConsult - Checking for overlaps with upcoming consultations for architect: {}, user: {}",
                request.getArchitectId(), userId);
        if (upcoming.stream().anyMatch(consult -> consult.getUserId().equals(userId))) {
            throw new BadRequestException("You already have an active booking with this architect.");
        }

        LocalDateTime newStart = request.getStartDate();
        LocalDateTime newEnd = request.getEndDate();

        log.info("Consultation Service : createConsult - Checking for overlaps wiht existing bookings");
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

        log.info("Consultation Service : createConsult - No overlaps detected. Proceeding to create consult");

        String location = "";

        // Check Location for offline consultations
        log.info("Consultation Service : createConsult - Checking consultation type: {}", request.getType());
        if (request.getType().equalsIgnoreCase("offline")) {
            if (request.getLocation() == null || request.getLocation().isBlank()) {

                throw new BadRequestException("Location is required for offline consultations.");
            }
            location = request.getLocation();
        }

        Consultation consult = Consultation.builder()
                .architectId(request.getArchitectId())
                .userId(userId)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .type(request.getType())
                .total(request.getTotal())
                .location(location)
                .status("waiting-for-payment")
                .build();

        return consultationRepository.save(consult).getId();
    }

    public List<Schedule> getArchitectSchedules(UUID architectId) {
        log.info("Consultation Service : getArchitectSchedules - Fetching schedules for architect: {}", architectId);

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

        log.info(
                "Consultation Service : getAllConsultsByArchitectId - Fetching consultations for architect: {}, type: {}, status: {}, includeCancelled: {}, upcoming: {}",
                architectId, type, status, includeCancelled, upcoming);

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

    public List<GetConsultationResponse> getAllConsults(String type, String status, Boolean includeCancelled,
            Boolean upcoming, UUID requestingUserId) {

        log.info(
                "Consultation Service : getAllConsults - Fetching all consultations with type: {}, status: {}, includeCancelled: {}, upcoming: {}, "
                        +
                        "requestingUserId: {}",
                type, status, includeCancelled, upcoming, requestingUserId);

        // Get base dataset
        List<Consultation> consults;

        // Handle includeCancelled filter at repository level for efficiency
        if (Boolean.TRUE.equals(includeCancelled)) {
            if (Boolean.TRUE.equals(upcoming)) {
                consults = consultationRepository
                        .findByStartDateGreaterThanEqualOrderByStartDate(LocalDateTime.now());
            } else {
                consults = consultationRepository.findAll(
                        Sort.by(Sort.Direction.ASC, "startDate"));
            }
        } else {
            if (Boolean.TRUE.equals(upcoming)) {
                consults = consultationRepository
                        .findByStatusNotAndStartDateGreaterThanEqualOrderByStartDate(
                                "cancelled", LocalDateTime.now());
            } else {
                consults = consultationRepository
                        .findByStatusNotOrderByStartDate("cancelled");
            }
        }

        List<Consultation> filtered = consults.stream()
                .filter(c -> type == null || type.isBlank() || type.equalsIgnoreCase(c.getType()))
                .filter(c -> status == null || status.isBlank() || status.equalsIgnoreCase(c.getStatus()))
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            return Collections.emptyList();
        }

        Set<UUID> architectIds = filtered.stream().map(Consultation::getArchitectId).collect(Collectors.toSet());
        List<Architect> architects = architectRepository.findAllByIdIn(architectIds);
        Map<UUID, Architect> architectMap = architects.stream().collect(Collectors.toMap(Architect::getId, a -> a));
        Map<UUID, GetUserNameAndCity> userMap = userApi.getAllUsersNameAndCity(requestingUserId);

        // Apply additional filters using streams
        return filtered.stream()
                .map(c -> {
                    GetUserNameAndCity u = userMap.get(c.getUserId());
                    Architect a = architectMap.get(c.getArchitectId());
                    return ConsultationUtils.toGetConsultationResponse(c, u, a);
                    // return GetConsultationResponse.builder()
                    // .consultation(c)
                    // .userName(u != null ? u.getUsername() : null)
                    // .userCity(u != null ? u.getCity() : null)
                    // .architectName(a != null ? a.getUsername() : null)
                    // .architectCity(a != null ? a.getCity() : null)
                    // .build();
                })
                .collect(Collectors.toList());
    }

    public List<UUID> getAllContactedArchitects(UUID userId) {

        log.info("Consultation Service : getAllContactedArchitects - Fetching all contacted architects for user: {}",
                userId);

        // Filter The consultation must be in a status "scheduled" and "in-progress"
        List<String> activeStatuses = List.of(
                ConsultationStatus.SCHEDULED.getStatus(),
                ConsultationStatus.IN_PROGRESS.getStatus());

        List<UUID> consultations = consultationRepository.findDistinctArchitectIdByUserIdAndStatusIn(userId,
                activeStatuses);

        return consultations;
    }

    public GetConsultationResponse getConsultById(UUID consultId) {

        log.info("Consultation Service : getConsultById - Fetching consultation: {}", consultId);

        Consultation consultation = consultationRepository.findById(consultId)
                .orElseThrow(() -> new BadRequestException("Consultation not found"));

        GetUserNameAndCity user = userApi.getUserNameAndCityById(consultation.getUserId());

        Architect architect = architectRepository.findById(consultation.getArchitectId())
                .orElseThrow(() -> new BadRequestException("Architect not found"));

        return ConsultationUtils.toGetConsultationResponse(consultation, user, architect);
    }

    public List<Consultation> getUserConsultations(UUID userId, String type, String status,
            Boolean includeCancelled, Boolean upcoming) {

        log.info(
                "Consultation Service : getUserConsultations - Fetching consultations for user: {}, type: {}, status: {}, includeCancelled: {}, upcoming: {}",
                userId, type, status, includeCancelled, upcoming);

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

    public UUID approveConsultation(UUID consultationId) {

        log.info("Consultation Service : approveConsultation - Approving consultation: {}", consultationId);

        Consultation consult = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new BadRequestException("Consultation not found"));

        CreateRoomRequest roomRequest = CreateRoomRequest.builder()
                .architectId(consult.getArchitectId())
                .userId(consult.getUserId())
                .startTime(consult.getStartDate())
                .endTime(consult.getEndDate())
                .build();

        UUID roomId = roomService.createRoom(roomRequest);

        log.info("Consultation Service : approveConsultation - Room created with ID: {}", roomId);

        consult.setStatus(ConsultationStatus.SCHEDULED.getStatus());
        consult.setRoomId(roomId);

        consultationRepository.save(consult);

        log.info("Consultation Service : approveConsultation - Consultation {} status updated to SCHEDULED",
                consultationId);

        confirmationService.notifyApproved(consultationId.toString());

        return roomId;
    }

    public void rejectConsultation(UUID consultationId, RejectConsultationRequest reason) {

        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new BadRequestException("Consultation not found"));

        CancellationReason reasonMessage = CancellationReason.fromString(reason.getMessage());

        Boolean consultationIsWaitingForConfirmationOrConsultationIsCancelled = consultation.getStatus()
                .equals(ConsultationStatus.WAITING_FOR_CONFIRMATION.getStatus()) ||
                consultation.getStatus().equals(ConsultationStatus.CANCELLED.getStatus());

        if (!consultationIsWaitingForConfirmationOrConsultationIsCancelled) {
            throw new BadRequestException("Consultation is not in a state that can be cancelled");
        }

        consultation.setStatus(ConsultationStatus.CANCELLED.getStatus());
        consultation.setReason(reason.getMessage());
        consultationRepository.save(consultation);

        confirmationService.notifyRejected(consultationId.toString(), reasonMessage);
    }

    public UUID userCancelConsultation(UUID consultationId, UUID userId) {

        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new BadRequestException("Consultation not found"));

        if (!consultation.getUserId().equals(userId)) {
            throw new BadRequestException("You are not authorized to cancel this consultation");
        }

        Boolean consultationIsScheduledOrInProgressOrEnded = consultation.getStatus()
                .equals(ConsultationStatus.SCHEDULED.getStatus()) ||
                consultation.getStatus().equals(ConsultationStatus.IN_PROGRESS.getStatus()) ||
                consultation.getStatus().equals(ConsultationStatus.ENDED.getStatus());

        if (consultationIsScheduledOrInProgressOrEnded) {
            throw new BadRequestException("Consultation is not in a state that can be cancelled");
        }

        consultation.setStatus(ConsultationStatus.CANCELLED.getStatus());
        consultation.setReason(CancellationReason.USER_CANCELLED.getReason());
        consultationRepository.save(consultation);

        return consultation.getId();
    }

    public void updateConsultation(UUID consultationId, UUID userId, UpdateConsultationRequest request) {
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new BadRequestException("Consultation not found"));

        if (!consultation.getUserId().equals(userId)) {
            throw new BadRequestException("You are not authorized to update this consultation");
        }

        Boolean consultationIsCancelledDueToArchitectUnavailable = consultation.getStatus()
                .equals(ConsultationStatus.CANCELLED.getStatus()) &&
                consultation.getReason().equals(CancellationReason.ARCHITECT_UNAVAILABLE.getReason());

        if (!consultationIsCancelledDueToArchitectUnavailable) {
            throw new BadRequestException("Consultation is not in a state that can be updated");
        }

        // Update fields based on the request
        if (request.getStartDate() != null) {
            if (request.getStartDate().isBefore(LocalDateTime.now())) {
                throw new BadRequestException("Start date cannot be in the past");
            }
            consultation.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            if (request.getEndDate().isBefore(LocalDateTime.now())) {
                throw new BadRequestException("End date cannot be in the past");
            }
            consultation.setEndDate(request.getEndDate());
        }

        // Validate date range
        if (request.getStartDate() != null && request.getEndDate() != null
                && request.getStartDate().isAfter(request.getEndDate())) {
            throw new BadRequestException("Start date cannot be after end date");
        }

        if (request.getType() != null) {
            consultation.setType(request.getType());
        }
        if (request.getLocation() != null) {
            consultation.setLocation(request.getLocation());
        }
        if (request.getTotal() != null) {
            consultation.setTotal(request.getTotal());
        }

        consultationRepository.save(consultation);
    }
}