package com.josev001.study_sync.service;

import com.josev001.study_sync.client.ClockifyClient;
import com.josev001.study_sync.dto.StudyEntryStoreResult;
import com.josev001.study_sync.dto.TimeEntryDto;
import com.josev001.study_sync.dto.TimeIntervalDto;
import com.josev001.study_sync.persistence.SyncRunRepository;
import com.josev001.study_sync.persistence.WeeklyStudyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FullHistoryImportTest {
    @Mock private ClockifyClient client;
    @Mock private StudyEntryService entries;
    @Mock private NotionService notion;
    @Mock private WeeklyStudyRepository weeks;
    @Mock private SyncRunRepository runs;

    private StudySyncService service() {
        return new StudySyncService(new ClockifyService(client), notion, weeks, runs, entries);
    }

    @Test
    void importsOldHistoryWithoutDuplicatesOrRunningTimersAndWithoutNotion() {
        TimeEntryDto old = entry("old", "2018-01-20T01:00:00Z", "PT2H");
        TimeEntryDto recent = entry("recent", "2026-09-01T12:00:00Z", "PT1H");
        TimeEntryDto running = entry("running", "2026-09-21T12:00:00Z", null);
        when(client.getTimeEntries(any(), any())).thenReturn(List.of(recent, old, old, running));
        when(entries.storeEntriesDetailed(List.of(recent, old)))
                .thenReturn(new StudyEntryStoreResult(1, 1, 0));

        var result = service().importAllStudyEntries();

        assertThat(result.from()).isEqualTo(LocalDate.of(2018, 1, 19));
        assertThat(result.to()).isEqualTo(LocalDate.now(ZoneId.of("America/Sao_Paulo")));
        assertThat(result.totalMinutes()).isEqualTo(180);
        assertThat(result.createdEntries()).isEqualTo(1);
        assertThat(result.updatedEntries()).isEqualTo(1);
        verify(client).getTimeEntries(eq(Instant.parse("1970-01-01T03:00:00Z")), any());
        verifyNoInteractions(notion, weeks, runs);
    }

    @Test
    void emptyAccountReturnsZeroAndValidRange() {
        when(client.getTimeEntries(any(), any())).thenReturn(List.of());
        when(entries.storeEntriesDetailed(List.of())).thenReturn(new StudyEntryStoreResult(0, 0, 0));

        var result = service().importAllStudyEntries();

        assertThat(result.processedEntries()).isZero();
        assertThat(result.totalMinutes()).isZero();
        assertThat(result.from()).isEqualTo(result.to());
        verifyNoInteractions(notion, weeks, runs);
    }

    @Test
    void connectionFailureDoesNotStorePartialHistory() {
        when(client.getTimeEntries(any(), any())).thenThrow(new RestClientException("Offline"));

        assertThatThrownBy(() -> service().importAllStudyEntries())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Verifique sua internet");
        verifyNoInteractions(entries, notion, weeks, runs);
    }

    private TimeEntryDto entry(String id, String start, String duration) {
        return new TimeEntryDto(id, "Study", "user", null, null, List.of(),
                new TimeIntervalDto(start, duration == null ? null : start, duration));
    }
}
