package com.josev001.study_sync.service;

import com.josev001.study_sync.dto.StudyGoalProgressDto;
import com.josev001.study_sync.persistence.StudyEntry;
import com.josev001.study_sync.persistence.StudyGoal;
import com.josev001.study_sync.persistence.StudyGoalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudyGoalServiceTest {

    private static final ZoneId APP_ZONE = ZoneId.of("America/Sao_Paulo");

    @Mock
    private StudyGoalRepository studyGoalRepository;

    @Mock
    private StudyEntryService studyEntryService;

    @Test
    void calculatesDailyAndWeeklyProgressFromStoredEntries() {
        Clock clock = Clock.fixed(Instant.parse("2026-09-18T15:00:00Z"), APP_ZONE);
        LocalDate today = LocalDate.of(2026, 9, 18);
        LocalDate weekStart = LocalDate.of(2026, 9, 14);
        StudyEntry todayEntry = entry("today", "2026-09-18T13:00:00Z", "2026-09-18T15:00:00Z");
        StudyEntry earlierEntry = entry("earlier", "2026-09-15T13:00:00Z", "2026-09-15T14:00:00Z");

        when(studyGoalRepository.findById((short) 1))
                .thenReturn(Optional.of(new StudyGoal(120, 600, Instant.now(clock))));
        when(studyEntryService.getEntriesBetween(today, today)).thenReturn(List.of(todayEntry));
        when(studyEntryService.getEntriesBetween(weekStart, weekStart.plusDays(6)))
                .thenReturn(List.of(todayEntry, earlierEntry));

        StudyGoalProgressDto progress = new StudyGoalService(studyGoalRepository, studyEntryService, clock)
                .getProgress();

        assertThat(progress.todayMinutes()).isEqualTo(120);
        assertThat(progress.weekMinutes()).isEqualTo(180);
        assertThat(progress.dailyProgressPercentage()).isEqualTo(100);
        assertThat(progress.weeklyProgressPercentage()).isEqualTo(30);
        assertThat(progress.dailyGoalReached()).isTrue();
        assertThat(progress.weeklyGoalReached()).isFalse();
    }

    private StudyEntry entry(String id, String start, String end) {
        Instant startedAt = Instant.parse(start);
        Instant endedAt = Instant.parse(end);
        return new StudyEntry(
                id,
                "project",
                "task",
                "Estudo",
                "Estudo",
                startedAt,
                endedAt,
                java.time.Duration.between(startedAt, endedAt).toMinutes(),
                startedAt.atZone(APP_ZONE).toLocalDate(),
                Instant.now()
        );
    }
}
