package com.josev001.study_sync.service;

import com.josev001.study_sync.dto.SyncResultDto;
import com.josev001.study_sync.dto.TimeEntryDto;
import com.josev001.study_sync.persistence.SyncRun;
import com.josev001.study_sync.persistence.SyncRunRepository;
import com.josev001.study_sync.persistence.WeeklyStudyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudySyncServiceTest {

    @Mock
    private ClockifyService clockifyService;

    @Mock
    private NotionService notionService;

    @Mock
    private WeeklyStudyRepository weeklyStudyRepository;

    @Mock
    private SyncRunRepository syncRunRepository;

    @Mock
    private StudyEntryService studyEntryService;

    @InjectMocks
    private StudySyncService studySyncService;

    @BeforeEach
    void setUp() {
        when(syncRunRepository.save(org.mockito.ArgumentMatchers.any(SyncRun.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(weeklyStudyRepository.findByWeekStart(org.mockito.ArgumentMatchers.any()))
                .thenReturn(Optional.empty());
        when(notionService.isConfigured()).thenReturn(true);
    }

    @Test
    void syncWeekUsesTheMondayOfTheProvidedWeek() {
        LocalDate monday = LocalDate.of(2026, 9, 7);
        Duration total = Duration.ofHours(12).plusMinutes(35);
        List<TimeEntryDto> entries = List.of();
        when(clockifyService.getStudyEntries(monday, monday.plusDays(6))).thenReturn(entries);
        when(clockifyService.getTotalStudyTime(entries)).thenReturn(total);
        when(notionService.updateWeekStudyTime(monday, total)).thenReturn("12:35H");

        SyncResultDto result = studySyncService.syncWeek(monday.plusDays(3));

        assertThat(result.weekStartDate()).isEqualTo(monday);
        assertThat(result.weekEndDate()).isEqualTo(monday.plusDays(6));
        assertThat(result.syncedTime()).isEqualTo("12:35H");
        assertThat(result.notionUpdated()).isTrue();
        verify(studyEntryService).storeEntries(entries);
        verify(notionService).updateWeekStudyTime(monday, total);
    }

    @Test
    void syncWeekStillStoresStudyTimeWithoutNotion() {
        LocalDate monday = LocalDate.of(2026, 9, 7);
        Duration total = Duration.ofHours(5).plusMinutes(20);
        List<TimeEntryDto> entries = List.of();
        when(notionService.isConfigured()).thenReturn(false);
        when(notionService.formatStudyTime(total)).thenReturn("5:20H");
        when(clockifyService.getStudyEntries(monday, monday.plusDays(6))).thenReturn(entries);
        when(clockifyService.getTotalStudyTime(entries)).thenReturn(total);

        SyncResultDto result = studySyncService.syncWeek(monday);

        assertThat(result.syncedTime()).isEqualTo("5:20H");
        assertThat(result.notionUpdated()).isFalse();
        verify(studyEntryService).storeEntries(entries);
        verify(notionService, never()).updateWeekStudyTime(monday, total);
        verify(notionService).formatStudyTime(total);
    }
}
