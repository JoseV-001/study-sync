package com.josev001.study_sync.service;

import com.josev001.study_sync.dto.ClockifyEntryMetadataDto;
import com.josev001.study_sync.dto.TimeEntryDto;
import com.josev001.study_sync.dto.TimeIntervalDto;
import com.josev001.study_sync.persistence.StudyEntry;
import com.josev001.study_sync.persistence.StudyEntryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudyEntryServiceTest {

    @Mock
    private StudyEntryRepository studyEntryRepository;

    @Mock
    private ClockifyMetadataService clockifyMetadataService;

    @Test
    void storesResolvedTopicAndTagsWithoutUsingRawIdsAsSubject() {
        TimeEntryDto entry = new TimeEntryDto(
                "entry-id",
                "Aula sobre indices",
                "user-id",
                "project-id",
                "task-id",
                List.of("tag-id"),
                new TimeIntervalDto("2026-09-15T22:00:00Z", "2026-09-15T23:00:00Z", "PT1H")
        );
        when(clockifyMetadataService.resolve(entry)).thenReturn(
                new ClockifyEntryMetadataDto("Estudo", "NoSQL", List.of("Faculdade"))
        );
        when(studyEntryRepository.findById("entry-id")).thenReturn(Optional.empty());

        int stored = new StudyEntryService(studyEntryRepository, clockifyMetadataService)
                .storeEntries(List.of(entry));

        ArgumentCaptor<StudyEntry> captor = ArgumentCaptor.forClass(StudyEntry.class);
        verify(studyEntryRepository).save(captor.capture());
        assertThat(stored).isEqualTo(1);
        assertThat(captor.getValue().getSubject()).isEqualTo("NoSQL");
        assertThat(captor.getValue().getProjectName()).isEqualTo("Estudo");
        assertThat(captor.getValue().getTopicName()).isEqualTo("NoSQL");
        assertThat(captor.getValue().getTagNames()).isEqualTo("Faculdade");
    }

    @Test
    void fallsBackToResolvedProjectNameWhenEntryHasNoTopicTagOrDescription() {
        TimeEntryDto entry = new TimeEntryDto(
                "entry-id",
                null,
                "user-id",
                "project-id",
                null,
                List.of(),
                new TimeIntervalDto("2026-09-15T22:00:00Z", "2026-09-15T23:00:00Z", "PT1H")
        );
        when(clockifyMetadataService.resolve(entry)).thenReturn(
                new ClockifyEntryMetadataDto("Estudo", null, List.of())
        );
        when(studyEntryRepository.findById("entry-id")).thenReturn(Optional.empty());

        new StudyEntryService(studyEntryRepository, clockifyMetadataService).storeEntries(List.of(entry));

        ArgumentCaptor<StudyEntry> captor = ArgumentCaptor.forClass(StudyEntry.class);
        verify(studyEntryRepository).save(captor.capture());
        assertThat(captor.getValue().getSubject()).isEqualTo("Estudo");
    }
}
