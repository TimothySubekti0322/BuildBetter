package com.buildbetter.consult.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.buildbetter.consult.dto.consult.Schedule;
import com.buildbetter.consult.model.Consult;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ConsultUtils {
    public static List<Schedule> createUpcomingSchedule(List<Consult> consults) {
        if (consults == null || consults.isEmpty()) {
            return Collections.emptyList();
        }

        // Filter out any sessions with missing dates, then sort by startDate
        List<Consult> valid = consults.stream()
                .filter(c -> c.getStartDate() != null && c.getEndDate() != null)
                .sorted(Comparator.comparing(Consult::getStartDate))
                .collect(Collectors.toList());

        // Group by the LocalDate of startDate
        Map<LocalDate, List<Consult>> byDate = valid.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getStartDate().toLocalDate(),
                        LinkedHashMap::new,
                        Collectors.toList()));

        List<Schedule> schedules = new ArrayList<>();
        for (Map.Entry<LocalDate, List<Consult>> entry : byDate.entrySet()) {
            LocalDate date = entry.getKey();
            List<Consult> sessions = entry.getValue();

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
}
