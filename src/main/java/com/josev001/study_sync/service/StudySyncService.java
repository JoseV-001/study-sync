package com.josev001.study_sync.service;

import com.josev001.study_sync.dto.SyncResultDto;
import com.josev001.study_sync.dto.SyncHistoryDto;
import com.josev001.study_sync.dto.WeeklyStudyDto;
import com.josev001.study_sync.dto.StudyImportDto;
import com.josev001.study_sync.dto.TimeEntryDto;
import com.josev001.study_sync.persistence.SyncRun;
import com.josev001.study_sync.persistence.SyncRunRepository;
import com.josev001.study_sync.persistence.WeeklyStudy;
import com.josev001.study_sync.persistence.WeeklyStudyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.Instant;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
public class StudySyncService {

    private static final Logger logger = LoggerFactory.getLogger(StudySyncService.class);
    private static final ZoneId APP_ZONE = ZoneId.of("America/Sao_Paulo");

    private final ClockifyService clockifyService;
    private final NotionService notionService;
    private final WeeklyStudyRepository weeklyStudyRepository;
    private final SyncRunRepository syncRunRepository;
    private final StudyEntryService studyEntryService;

    @Value("${study-sync.retry.max-attempts:3}")
    private int retryMaxAttempts;

    @Value("${study-sync.retry.delay-ms:30000}")
    private long retryDelayMs;

    public StudySyncService(
            ClockifyService clockifyService,
            NotionService notionService,
            WeeklyStudyRepository weeklyStudyRepository,
            SyncRunRepository syncRunRepository,
            StudyEntryService studyEntryService
    ) {
        this.clockifyService = clockifyService;
        this.notionService = notionService;
        this.weeklyStudyRepository = weeklyStudyRepository;
        this.syncRunRepository = syncRunRepository;
        this.studyEntryService = studyEntryService;
    }

    @Transactional(noRollbackFor = RuntimeException.class)
    public SyncResultDto syncCurrentWeek() {
        return syncWeekWithRetry(getStartOfWeek(LocalDate.now(APP_ZONE)), "manual");
    }

    @Transactional(noRollbackFor = RuntimeException.class)
    public SyncResultDto syncPreviousWeek() {
        return syncWeekWithRetry(getStartOfWeek(LocalDate.now(APP_ZONE)).minusWeeks(1), "manual");
    }

    @Transactional(noRollbackFor = RuntimeException.class)
    public SyncResultDto syncWeek(LocalDate dateInWeek) {
        return syncWeekWithRetry(dateInWeek, "manual");
    }

    @Transactional(noRollbackFor = RuntimeException.class)
    public SyncResultDto syncWeekWithRetry(LocalDate dateInWeek, String triggeredBy) {
        RuntimeException lastException = null;
        int maxAttempts = Math.max(1, retryMaxAttempts);

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return syncWeek(dateInWeek, triggeredBy + " (attempt " + attempt + ")");
            } catch (RuntimeException exception) {
                lastException = exception;
                if (attempt < maxAttempts) {
                    logger.warn(
                            "Sync attempt {} of {} failed. Retrying in {} ms",
                            attempt,
                            maxAttempts,
                            retryDelayMs,
                            exception
                    );
                    try {
                        Thread.sleep(retryDelayMs);
                    } catch (InterruptedException interruptedException) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException("Sync retry was interrupted", interruptedException);
                    }
                }
            }
        }

        throw lastException;
    }

    @Transactional(noRollbackFor = RuntimeException.class)
    public SyncResultDto syncWeek(LocalDate dateInWeek, String triggeredBy) {
        LocalDate startOfWeek = getStartOfWeek(dateInWeek);
        SyncRun syncRun = syncRunRepository.save(
                new SyncRun(startOfWeek, triggeredBy, Instant.now())
        );

        try {
            List<TimeEntryDto> entries = clockifyService.getStudyEntries(startOfWeek, startOfWeek.plusDays(6));
            studyEntryService.storeEntries(entries);
            Duration totalStudyTime = clockifyService.getTotalStudyTime(entries);
            String syncedTime = notionService.isConfigured()
                    ? notionService.updateWeekStudyTime(startOfWeek, totalStudyTime)
                    : notionService.formatStudyTime(totalStudyTime);
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

    public List<SyncHistoryDto> getSyncHistory() {
        return syncRunRepository.findTop50ByOrderByCreatedAtDesc()
                .stream()
                .map(syncRun -> new SyncHistoryDto(
                        syncRun.getId(),
                        syncRun.getWeekStart(),
                        syncRun.getWeekStart().plusDays(6),
                        syncRun.getTriggeredBy(),
                        syncRun.getStatus(),
                        syncRun.getTotalMinutes(),
                        syncRun.getErrorMessage(),
                        syncRun.getCreatedAt(),
                        syncRun.getFinishedAt()
                ))
                .toList();
    }

    public List<WeeklyStudyDto> getWeeklyStudyHistory(LocalDate from, LocalDate to) {
        return weeklyStudyRepository.findByWeekStartBetweenOrderByWeekStartDesc(from, to)
                .stream()
                .map(weeklyStudy -> new WeeklyStudyDto(
                        weeklyStudy.getWeekStart(),
                        weeklyStudy.getWeekEnd(),
                        weeklyStudy.getTotalMinutes(),
                        weeklyStudy.getNotionTime(),
                        weeklyStudy.getSyncedAt()
                ))
                .toList();
    }

    @Transactional
    public StudyImportDto importStudyEntries(LocalDate from, LocalDate to) {
        List<TimeEntryDto> entries = clockifyService.getStudyEntries(from, to);
        int importedEntries = studyEntryService.storeEntries(entries);
        long totalMinutes = clockifyService.getTotalStudyTime(entries).toMinutes();
        return new StudyImportDto(from, to, importedEntries, totalMinutes);
    }

    private LocalDate getStartOfWeek(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }
}
