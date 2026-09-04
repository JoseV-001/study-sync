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

    private final ClockifyClient clockifyClient;

    public ClockifyService(ClockifyClient clockifyClient) {
        this.clockifyClient = clockifyClient;
    }

    // Soma a duração dos registros finalizados da semana atual.
    public Duration getTotalStudyTime() {

        LocalDate startOfWeek = getStartOfCurrentWeek();
        LocalDate endOfWeek = getEndOfCurrentWeek();

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
                .atZone(ZoneId.of("America/Sao_Paulo"))
                .toLocalDate();
    }

    // Retorna a segunda-feira da semana atual.
    private LocalDate getStartOfCurrentWeek() {
        return LocalDate.now(ZoneId.of("America/Sao_Paulo"))
                .with(
                        TemporalAdjusters.previousOrSame(
                                DayOfWeek.MONDAY
                        )
                );
    }

    // Retorna o domingo da semana atual.
    private LocalDate getEndOfCurrentWeek() {
        return LocalDate.now(ZoneId.of("America/Sao_Paulo"))
                .with(
                        TemporalAdjusters.nextOrSame(
                                DayOfWeek.SUNDAY
                        )
                );
    }
}