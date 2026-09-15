package com.josev001.study_sync.service;

import com.josev001.study_sync.dto.SyncResultDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudySyncServiceTest {

    @Mock
    private ClockifyService clockifyService;

    @Mock
    private NotionService notionService;

    @InjectMocks
    private StudySyncService studySyncService;

    @Test
    void syncWeekUsesTheMondayOfTheProvidedWeek() {
        LocalDate monday = LocalDate.of(2026, 9, 7);
        Duration total = Duration.ofHours(12).plusMinutes(35);
        when(clockifyService.getTotalStudyTime(monday)).thenReturn(total);
        when(notionService.updateWeekStudyTime(monday, total)).thenReturn("12:35H");

        SyncResultDto result = studySyncService.syncWeek(monday.plusDays(3));

        assertThat(result.weekStartDate()).isEqualTo(monday);
        assertThat(result.weekEndDate()).isEqualTo(monday.plusDays(6));
        assertThat(result.syncedTime()).isEqualTo("12:35H");
        verify(clockifyService).getTotalStudyTime(monday);
        verify(notionService).updateWeekStudyTime(monday, total);
    }
}
