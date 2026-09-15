package com.josev001.study_sync.service;

import com.josev001.study_sync.dto.SyncResultDto;
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

import static org.assertj.core.api.Assertions.assertThat;
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

    @InjectMocks
    private StudySyncService studySyncService;

    @BeforeEach
    void setUp() {
        when(syncRunRepository.save(org.mockito.ArgumentMatchers.any(SyncRun.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(weeklyStudyRepository.findByWeekStart(org.mockito.ArgumentMatchers.any()))
                .thenReturn(Optional.empty());
    }

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
