package com.josev001.study_sync.service;

import com.josev001.study_sync.client.ClockifyClient;
import com.josev001.study_sync.dto.TimeEntryDto;
import com.josev001.study_sync.dto.TimeIntervalDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClockifyServiceTest {

    @Mock
    private ClockifyClient clockifyClient;

    @Test
    void getTotalStudyTimeFiltersEntriesByWeekAndFinalizedDuration() {
        LocalDate monday = LocalDate.of(2026, 9, 7);
        TimeEntryDto inWeek = entry("2026-09-10T12:00:00Z", "PT2H30M");
        TimeEntryDto outsideWeek = entry("2026-09-14T12:00:00Z", "PT8H");
        TimeEntryDto running = entry("2026-09-11T12:00:00Z", null);
        when(clockifyClient.getTimeEntries(any(), any())).thenReturn(List.of(inWeek, outsideWeek, running));

        Duration result = new ClockifyService(clockifyClient).getTotalStudyTime(monday);

        assertThat(result).isEqualTo(Duration.ofHours(2).plusMinutes(30));
        verify(clockifyClient).getTimeEntries(
                Instant.parse("2026-09-07T03:00:00Z"),
                Instant.parse("2026-09-14T03:00:00Z")
        );
    }

    @Test
    void removesDuplicatedClockifyEntriesBeforeProcessing() {
        TimeEntryDto duplicated = entry("2026-09-10T12:00:00Z", "PT2H");
        when(clockifyClient.getTimeEntries(any(), any())).thenReturn(List.of(duplicated, duplicated));

        List<TimeEntryDto> result = new ClockifyService(clockifyClient)
                .getStudyEntries(LocalDate.of(2026, 9, 7), LocalDate.of(2026, 9, 13));

        assertThat(result).hasSize(1);
    }

    private TimeEntryDto entry(String start, String duration) {
        return new TimeEntryDto(
                "id",
                "study",
                "user",
                "project",
                "task",
                List.of("tag"),
                new TimeIntervalDto(start, start, duration)
        );
    }
}
