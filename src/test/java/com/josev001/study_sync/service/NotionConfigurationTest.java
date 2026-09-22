package com.josev001.study_sync.service;

import com.josev001.study_sync.client.NotionClient;
import com.josev001.study_sync.config.ClockifyProperties;
import com.josev001.study_sync.config.NotionProperties;
import com.josev001.study_sync.persistence.AppSettingRepository;
import com.josev001.study_sync.persistence.SyncRun;
import com.josev001.study_sync.persistence.SyncRunRepository;
import com.josev001.study_sync.persistence.SyncRunStatus;
import com.josev001.study_sync.persistence.WeeklyStudy;
import com.josev001.study_sync.persistence.WeeklyStudyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class NotionConfigurationTest {
    @ParameterizedTest
    @CsvSource({"test-key,,false", ",source-id,false", "test-key,source-id,true"})
    void dashboardAndSyncAgreeOnRequiredNotionConfiguration(String key, String source, boolean configured) {
        IntegrationSettingsService settings = settings(key, source);
        NotionService notion = new NotionService(mock(NotionClient.class), settings);

        assertThat(settings.getSettings().notionConfigured()).isEqualTo(configured);
        assertThat(notion.isConfigured()).isEqualTo(configured);
    }

    @Test
    void weeklySyncSavesLocalHistoryWhenOnlyTheNotionKeyExists() {
        NotionClient client = mock(NotionClient.class);
        NotionService notion = new NotionService(client, settings("test-key", null));
        ClockifyService clockify = mock(ClockifyService.class);
        StudyEntryService entries = mock(StudyEntryService.class);
        WeeklyStudyRepository weeks = mock(WeeklyStudyRepository.class);
        SyncRunRepository runs = mock(SyncRunRepository.class);
        when(runs.save(any(SyncRun.class))).thenAnswer(call -> call.getArgument(0));
        LocalDate monday = LocalDate.of(2026, 9, 14);
        when(clockify.getStudyEntries(monday, monday.plusDays(6))).thenReturn(List.of());
        when(clockify.getTotalStudyTime(List.of())).thenReturn(Duration.ofMinutes(123));
        StudySyncService service = new StudySyncService(clockify, notion, weeks, runs, entries);

        assertThat(service.syncWeek(monday, "test").syncedTime()).isEqualTo("2:03H");

        verify(entries).storeEntries(List.of());
        verify(weeks).save(argThat((WeeklyStudy week) -> week.getTotalMinutes() == 123));
        verify(runs, atLeastOnce()).save(argThat(run -> run.getStatus() == SyncRunStatus.SUCCESS));
        verifyNoInteractions(client);
    }

    private IntegrationSettingsService settings(String key, String source) {
        NotionProperties properties = new NotionProperties();
        properties.setApiKey(key);
        properties.setDataSourceId(source);
        return new IntegrationSettingsService(mock(AppSettingRepository.class), new ClockifyProperties(), properties);
    }
}
