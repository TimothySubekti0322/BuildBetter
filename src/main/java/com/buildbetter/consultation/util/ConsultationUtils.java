package com.buildbetter.consultation.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.buildbetter.consultation.dto.consultation.GetConsultationResponse;
import com.buildbetter.consultation.dto.consultation.Schedule;
import com.buildbetter.consultation.model.Architect;
import com.buildbetter.consultation.model.Consultation;
import com.buildbetter.user.dto.user.GetUserNameAndCity;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ConsultationUtils {
    public static List<Schedule> createUpcomingSchedule(List<Consultation> consults) {
        if (consults == null || consults.isEmpty()) {
            return Collections.emptyList();
        }

        // Filter out any sessions with missing dates, then sort by startDate
        List<Consultation> valid = consults.stream()
                .filter(c -> c.getStartDate() != null && c.getEndDate() != null)
                .sorted(Comparator.comparing(Consultation::getStartDate))
                .collect(Collectors.toList());

        // Group by the LocalDate of startDate
        Map<LocalDate, List<Consultation>> byDate = valid.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getStartDate().toLocalDate(),
                        LinkedHashMap::new,
                        Collectors.toList()));

        List<Schedule> schedules = new ArrayList<>();
        for (Map.Entry<LocalDate, List<Consultation>> entry : byDate.entrySet()) {
            LocalDate date = entry.getKey();
            List<Consultation> sessions = entry.getValue();

            // For each consult, extend its window if it's offline
            List<LocalTime> slots = sessions.stream()
                    .flatMap(c -> {
                        LocalTime start = c.getStartDate().toLocalTime();
                        LocalTime end = c.getEndDate().toLocalTime();

                        if ("offline".equalsIgnoreCase(c.getType())) {
                            // apply 1-hour buffer
                            start = start.minusHours(1);
                            end = end.plusHours(1);
                        }

                        return generate30MinutesTimeSlots(start, end).stream();
                    })
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

            schedules.add(new Schedule(date, slots));
        }

        return schedules;
    }

    private static List<LocalTime> generate30MinutesTimeSlots(LocalTime start, LocalTime end) {
        List<LocalTime> slots = new ArrayList<>();
        LocalTime current = start;
        while (current.isBefore(end)) {
            slots.add(current);
            current = current.plusMinutes(30);
        }
        return slots;
    }

    public static GetConsultationResponse toGetConsultationResponse(Consultation consultation, GetUserNameAndCity user,
            Architect architect) {
        return GetConsultationResponse.builder()
                .id(consultation.getId())
                .userId(user.getId())
                .userName(user.getUsername())
                .userCity(user.getCity())
                .architectId(architect.getId())
                .architectName(architect.getUsername())
                .architectCity(architect.getCity())
                .roomId(consultation.getRoomId())
                .type(consultation.getType())
                .total(consultation.getTotal())
                .status(consultation.getStatus())
                .reason(consultation.getReason())
                .location(consultation.getLocation())
                .startDate(consultation.getStartDate())
                .endDate(consultation.getEndDate())
                .createdAt(consultation.getCreatedAt())
                .build();
    }
}
