package com.josev001.study_sync.service;

import com.josev001.study_sync.dto.SyncResultDto;
import com.josev001.study_sync.persistence.SyncRun;
import com.josev001.study_sync.persistence.SyncRunRepository;
import com.josev001.study_sync.persistence.WeeklyStudy;
import com.josev001.study_sync.persistence.WeeklyStudyRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.Instant;
import java.time.temporal.TemporalAdjusters;

@Service
public class StudySyncService {

    private static final ZoneId APP_ZONE = ZoneId.of("America/Sao_Paulo");

    private final ClockifyService clockifyService;
    private final NotionService notionService;
    private final WeeklyStudyRepository weeklyStudyRepository;
    private final SyncRunRepository syncRunRepository;

    public StudySyncService(
            ClockifyService clockifyService,
            NotionService notionService,
            WeeklyStudyRepository weeklyStudyRepository,
            SyncRunRepository syncRunRepository
    ) {
        this.clockifyService = clockifyService;
        this.notionService = notionService;
        this.weeklyStudyRepository = weeklyStudyRepository;
        this.syncRunRepository = syncRunRepository;
    }

    public SyncResultDto syncCurrentWeek() {
        return syncWeek(getStartOfWeek(LocalDate.now(APP_ZONE)), "manual");
    }

    public SyncResultDto syncPreviousWeek() {
        return syncWeek(getStartOfWeek(LocalDate.now(APP_ZONE)).minusWeeks(1), "manual");
    }

    public SyncResultDto syncWeek(LocalDate dateInWeek) {
        return syncWeek(dateInWeek, "manual");
    }

    @Transactional
    public SyncResultDto syncWeek(LocalDate dateInWeek, String triggeredBy) {
        LocalDate startOfWeek = getStartOfWeek(dateInWeek);
        SyncRun syncRun = syncRunRepository.save(
                new SyncRun(startOfWeek, triggeredBy, Instant.now())
        );

        try {
            Duration totalStudyTime = clockifyService.getTotalStudyTime(startOfWeek);
            String syncedTime = notionService.updateWeekStudyTime(startOfWeek, totalStudyTime);
            long totalMinutes = totalStudyTime.toMinutes();
            Instant syncedAt = Instant.now();

            weeklyStudyRepository.findByWeekStart(startOfWeek)
                    .ifPresentOrElse(
                            weeklyStudy -> weeklyStudy.update(totalMinutes, syncedTime, syncedAt),
                            () -> weeklyStudyRepository.save(new WeeklyStudy(
                                    startOfWeek,
                                    startOfWeek.plusDays(6),
                                    totalMinutes,
                                    syncedTime,
                                    syncedAt
                            ))
                    );
            syncRun.markSuccess(totalMinutes, syncedAt);
            syncRunRepository.save(syncRun);

            return new SyncResultDto(startOfWeek, startOfWeek.plusDays(6), syncedTime);
        } catch (RuntimeException exception) {
            syncRun.markFailed(exception.getMessage(), Instant.now());
            syncRunRepository.save(syncRun);
            throw exception;
        }
    }

    private LocalDate getStartOfWeek(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }
}
