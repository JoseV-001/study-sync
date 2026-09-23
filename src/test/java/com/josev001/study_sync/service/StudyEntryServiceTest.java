package com.josev001.study_sync.service;

import com.josev001.study_sync.dto.ClockifyEntryMetadataDto;
import com.josev001.study_sync.dto.TimeEntryDto;
import com.josev001.study_sync.dto.TimeIntervalDto;
import com.josev001.study_sync.dto.StudyEntryStoreResult;
import com.josev001.study_sync.dto.SubjectSuggestionDto;
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
import static org.mockito.Mockito.never;
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

    @Test
    void updatesExistingEntryWhenThePeriodIsReprocessed() {
        TimeEntryDto entry = new TimeEntryDto(
                "entry-id",
                "Estudo atualizado",
                "user-id",
                "project-id",
                "task-id",
                List.of(),
                new TimeIntervalDto("2026-09-15T22:00:00Z", "2026-09-15T23:30:00Z", "PT1H30M")
        );
        StudyEntry existing = new StudyEntry(
                "entry-id", "project-id", "task-id", null, null, null, null,
                "Estudo antigo", "Estudo antigo", java.time.Instant.parse("2026-09-15T22:00:00Z"),
                java.time.Instant.parse("2026-09-15T23:00:00Z"), 60,
                java.time.LocalDate.of(2026, 9, 15), java.time.Instant.now()
        );
        when(clockifyMetadataService.resolve(entry)).thenReturn(
                new ClockifyEntryMetadataDto("Estudo", "Java", List.of())
        );
        when(studyEntryRepository.findById("entry-id")).thenReturn(Optional.of(existing));

        StudyEntryStoreResult result = new StudyEntryService(studyEntryRepository, clockifyMetadataService)
                .storeEntriesDetailed(List.of(entry));

        assertThat(result.createdEntries()).isZero();
        assertThat(result.updatedEntries()).isEqualTo(1);
        assertThat(result.skippedEntries()).isZero();
        assertThat(existing.getSubject()).isEqualTo("Java");
        verify(studyEntryRepository, never()).save(existing);
    }

    @Test
    void listsKnownSubjectsByCategoryWithoutLooseDescriptions() {
        when(studyEntryRepository.findDistinctTopicSubjects()).thenReturn(List.of(" Java ", "Algoritmos"));
        when(studyEntryRepository.findDistinctTagSubjects()).thenReturn(List.of("Faculdade", "java"));
        when(studyEntryRepository.findDistinctProjectSubjects()).thenReturn(List.of("Study Sync"));

        List<SubjectSuggestionDto> subjects = new StudyEntryService(studyEntryRepository, clockifyMetadataService)
                .getKnownSubjects();

        assertThat(subjects).containsExactly(
                new SubjectSuggestionDto("Algoritmos", "Topico"),
                new SubjectSuggestionDto("Java", "Topico"),
                new SubjectSuggestionDto("Faculdade", "Tag"),
                new SubjectSuggestionDto("Study Sync", "Projeto")
        );
    }
}
