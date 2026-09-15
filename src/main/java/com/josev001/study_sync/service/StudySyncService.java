package com.josev001.study_sync.service;

import com.josev001.study_sync.dto.SyncResultDto;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;

@Service
public class StudySyncService {

    private static final ZoneId APP_ZONE = ZoneId.of("America/Sao_Paulo");

    private final ClockifyService clockifyService;
    private final NotionService notionService;

    public StudySyncService(
            ClockifyService clockifyService,
            NotionService notionService
    ) {
        this.clockifyService = clockifyService;
        this.notionService = notionService;
    }

    public SyncResultDto syncCurrentWeek() {
        return syncWeek(getStartOfWeek(LocalDate.now(APP_ZONE)));
    }

    public SyncResultDto syncPreviousWeek() {
        return syncWeek(getStartOfWeek(LocalDate.now(APP_ZONE)).minusWeeks(1));
    }

    public SyncResultDto syncWeek(LocalDate dateInWeek) {
        LocalDate startOfWeek = getStartOfWeek(dateInWeek);
        Duration totalStudyTime = clockifyService.getTotalStudyTime(startOfWeek);
        String syncedTime = notionService.updateWeekStudyTime(startOfWeek, totalStudyTime);

        return new SyncResultDto(
                startOfWeek,
                startOfWeek.plusDays(6),
                syncedTime
        );
    }

    private LocalDate getStartOfWeek(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }
}
