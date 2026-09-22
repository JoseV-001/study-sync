package com.josev001.study_sync.service;

import com.josev001.study_sync.client.NotionClient;
import com.josev001.study_sync.dto.NotionPageDto;
import com.josev001.study_sync.dto.NotionQueryResponseDto;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;

@Service
public class NotionService {

    private final NotionClient notionClient;
    private final IntegrationSettingsService settingsService;

    public NotionService(NotionClient notionClient, IntegrationSettingsService settingsService) {
        this.notionClient = notionClient;
        this.settingsService = settingsService;
    }

    public boolean isConfigured() {
        return settingsService.isNotionConfigured();
    }

    public String updateCurrentWeekStudyTime(Duration totalStudyTime) {
        LocalDate startOfWeek = LocalDate.now(ZoneId.of("America/Sao_Paulo"))
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return updateWeekStudyTime(startOfWeek, totalStudyTime);
    }

    public String updateWeekStudyTime(LocalDate startOfWeek, Duration totalStudyTime) {

        NotionQueryResponseDto response =
                notionClient.queryWeeklyPlanning(startOfWeek);

        if (response == null
                || response.results() == null
                || response.results().isEmpty()) {

            throw new IllegalStateException(
                    "Nenhum planejamento encontrado para a semana: "
                            + startOfWeek
            );
        }

        NotionPageDto currentWeek = response.results().get(0);

        String formattedTime = formatStudyTime(totalStudyTime);

        notionClient.updateStudyHours(
                currentWeek.id(),
                formattedTime
        );

        return formattedTime;
    }

    // Converte a duração para o padrão usado no Notion: 12:35H.
    public String formatStudyTime(Duration duration) {

        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();

        return String.format(
                "%d:%02dH",
                hours,
                minutes
        );
    }
}
