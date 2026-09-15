package com.josev001.study_sync.service;

import com.josev001.study_sync.client.ClockifyClient;
import com.josev001.study_sync.dto.TimeEntryDto;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;

@Service
public class ClockifyService {

    private static final ZoneId APP_ZONE = ZoneId.of("America/Sao_Paulo");

    private final ClockifyClient clockifyClient;

    public ClockifyService(ClockifyClient clockifyClient) {
        this.clockifyClient = clockifyClient;
    }

    public Duration getTotalStudyTime() {
        LocalDate startOfWeek = LocalDate.now(APP_ZONE)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return getTotalStudyTime(startOfWeek);
    }

    // Soma os registros finalizados de uma semana iniciada na segunda-feira.
    public Duration getTotalStudyTime(LocalDate startOfWeek) {
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        return clockifyClient.getTimeEntries()
                .stream()
                .filter(entry -> entry.timeInterval().duration() != null)
                .filter(entry -> {

                    LocalDate entryDate = getEntryDate(entry);

                    // Segunda-feira <= data do registro <= domingo.
                    return !entryDate.isBefore(startOfWeek)
                            && !entryDate.isAfter(endOfWeek);
                })
                .map(entry ->
                        Duration.parse(entry.timeInterval().duration())
                )
                .reduce(Duration.ZERO, Duration::plus);
    }

    // Converte a data/hora UTC do Clockify para a data no fuso de São Paulo.
    private LocalDate getEntryDate(TimeEntryDto entry) {

        Instant start =
                Instant.parse(entry.timeInterval().start());

        return start
                .atZone(APP_ZONE)
                .toLocalDate();
    }
}
