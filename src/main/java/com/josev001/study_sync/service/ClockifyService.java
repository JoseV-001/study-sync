package com.josev001.study_sync.service;

import com.josev001.study_sync.client.ClockifyClient;
import com.josev001.study_sync.dto.TimeEntryDto;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.Duration;
import java.util.List;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;

@Service
public class ClockifyService {

    private final ClockifyClient clockifyClient;

    public ClockifyService(ClockifyClient clockifyClient) {
        this.clockifyClient = clockifyClient;
    }

    public List<TimeEntryDto> getTimeEntries() {
        return clockifyClient.getTimeEntries();
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

                    return !entryDate.isBefore(startOfWeek)//A data do registro não pode ser anterior à segunda-feira da semana atual
                            && !entryDate.isAfter(endOfWeek);// a data não pode ser depois do domingo
                    //segunda <= data <= domingo
                })
                .map(entry -> Duration.parse(entry.timeInterval().duration()))
                .reduce(Duration.ZERO, Duration::plus);
    }

    // Converte a data/hora UTC recebida do Clockify para uma data no fuso de São Paulo.
    private LocalDate getEntryDate(TimeEntryDto entry) {

        Instant start = Instant.parse(entry.timeInterval().start());

        return start
                .atZone(ZoneId.of("America/Sao_Paulo"))
                .toLocalDate();
    }

    // Retorna a segunda-feira da semana atual.
    private LocalDate getStartOfCurrentWeek() {
        return LocalDate.now(ZoneId.of("America/Sao_Paulo"))
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    // Retorna o domingo da semana atual.
    private LocalDate getEndOfCurrentWeek() {
        return LocalDate.now(ZoneId.of("America/Sao_Paulo"))
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }
}