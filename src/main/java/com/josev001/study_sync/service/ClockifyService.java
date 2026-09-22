package com.josev001.study_sync.service;

import com.josev001.study_sync.client.ClockifyClient;
import com.josev001.study_sync.dto.TimeEntryDto;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

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
        return getTotalStudyTime(getStudyEntries(startOfWeek, startOfWeek.plusDays(6)));
    }

    public List<TimeEntryDto> getAllStudyEntries() {
        // An explicit start avoids Clockify's default recent-history window.
        return getStudyEntries(LocalDate.of(1970, 1, 1), LocalDate.now(APP_ZONE));
    }

    public List<TimeEntryDto> getStudyEntries(LocalDate from, LocalDate to) {
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("The end date must not be before the start date");
        }

        Instant rangeStart = from.atStartOfDay(APP_ZONE).toInstant();
        Instant rangeEnd = to.plusDays(1).atStartOfDay(APP_ZONE).toInstant();
        try {
            Map<String, TimeEntryDto> uniqueEntries = new LinkedHashMap<>();
            clockifyClient.getTimeEntries(rangeStart, rangeEnd)
                .forEach(entry -> uniqueEntries.putIfAbsent(entry.id(), entry));
            return uniqueEntries.values().stream()
                .filter(entry -> entry.timeInterval().duration() != null)
                .filter(entry -> {
                    LocalDate entryDate = getEntryDate(entry);
                    return !entryDate.isBefore(from) && !entryDate.isAfter(to);
                })
                .toList();
        } catch (RestClientResponseException exception) {
            throw new IllegalStateException(getApiErrorMessage(exception), exception);
        } catch (RestClientException exception) {
            throw new IllegalStateException("Nao foi possivel conectar ao Clockify. Verifique sua internet e tente novamente.", exception);
        }
    }

    public Duration getTotalStudyTime(List<TimeEntryDto> entries) {
        return entries.stream()
                .map(entry -> Duration.parse(entry.timeInterval().duration()))
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

    private String getApiErrorMessage(RestClientResponseException exception) {
        return switch (exception.getStatusCode().value()) {
            case 401, 403 -> "O Clockify rejeitou a chave da API. Confira a chave nas configuracoes.";
            case 429 -> "O Clockify limitou as requisicoes. Aguarde um pouco e tente novamente.";
            default -> "O Clockify nao conseguiu atender a importacao agora. Tente novamente mais tarde.";
        };
    }
}
