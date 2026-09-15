package com.josev001.study_sync.service;

import com.josev001.study_sync.dto.StudyAnalyticsDto;
import com.josev001.study_sync.persistence.StudyEntry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudyAnalyticsServiceTest {

    @Mock
    private StudyEntryService studyEntryService;

    @InjectMocks
    private StudyAnalyticsService studyAnalyticsService;

    @Test
    void calculatesInsightsFromStoredEntries() {
        LocalDate from = LocalDate.of(2026, 9, 7);
        LocalDate to = LocalDate.of(2026, 9, 13);
        when(studyEntryService.getEntriesBetween(from, to)).thenReturn(List.of(
                entry("1", "Matematica", "2026-09-07T12:00:00Z", "2026-09-07T14:00:00Z"),
                entry("2", "Java", "2026-09-08T22:00:00Z", "2026-09-08T23:00:00Z"),
                entry("3", "Matematica", "2026-09-09T13:00:00Z", "2026-09-09T14:00:00Z")
        ));

        StudyAnalyticsDto analytics = studyAnalyticsService.getAnalytics(from, to);

        assertThat(analytics.totalMinutes()).isEqualTo(240);
        assertThat(analytics.activeDays()).isEqualTo(3);
        assertThat(analytics.mostStudiedDay().label()).isEqualTo("Segunda");
        assertThat(analytics.topSubject().label()).isEqualTo("Matematica");
        assertThat(analytics.peakStudyHour().label()).isEqualTo("10h - 11h");
    }

    private StudyEntry entry(String id, String subject, String start, String end) {
        Instant startedAt = Instant.parse(start);
        Instant endedAt = Instant.parse(end);
        return new StudyEntry(
                id,
                "project",
                "task",
                subject,
                subject,
                startedAt,
                endedAt,
                java.time.Duration.between(startedAt, endedAt).toMinutes(),
                startedAt.atZone(java.time.ZoneId.of("America/Sao_Paulo")).toLocalDate(),
                Instant.now()
        );
    }
}
