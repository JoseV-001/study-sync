package com.josev001.study_sync.service;

import com.josev001.study_sync.dto.SubjectGoalProgressDto;
import com.josev001.study_sync.persistence.StudyEntry;
import com.josev001.study_sync.persistence.SubjectGoal;
import com.josev001.study_sync.persistence.SubjectGoalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubjectGoalServiceTest {

    private static final ZoneId APP_ZONE = ZoneId.of("America/Sao_Paulo");

    @Mock
    private SubjectGoalRepository subjectGoalRepository;

    @Mock
    private StudyEntryService studyEntryService;

    @Test
    void calculatesWeeklyProgressForEachSubjectGoal() {
        Clock clock = Clock.fixed(Instant.parse("2026-09-18T15:00:00Z"), APP_ZONE);
        LocalDate weekStart = LocalDate.of(2026, 9, 14);
        when(subjectGoalRepository.findAllByOrderBySubjectAsc()).thenReturn(List.of(
                new SubjectGoal("Java", 300, Instant.now(clock))
        ));
        when(studyEntryService.getEntriesBetween(weekStart, weekStart.plusDays(6))).thenReturn(List.of(
                entry("java-study", "Java", "2026-09-18T13:00:00Z", "2026-09-18T15:00:00Z")
        ));

        List<SubjectGoalProgressDto> progress = new SubjectGoalService(
                subjectGoalRepository,
                studyEntryService,
                clock
        ).getProgress();

        assertThat(progress).hasSize(1);
        assertThat(progress.getFirst().subject()).isEqualTo("Java");
        assertThat(progress.getFirst().studiedMinutes()).isEqualTo(120);
        assertThat(progress.getFirst().progressPercentage()).isEqualTo(40);
        assertThat(progress.getFirst().goalReached()).isFalse();
    }

    @Test
    void countsStudyForAProjectTagAndTopicGoalWhenTheEntryHasAllIdentifiers() {
        Clock clock = Clock.fixed(Instant.parse("2026-09-18T15:00:00Z"), APP_ZONE);
        LocalDate weekStart = LocalDate.of(2026, 9, 14);
        when(subjectGoalRepository.findAllByOrderBySubjectAsc()).thenReturn(List.of(
                new SubjectGoal("DevSuperior", 300, Instant.now(clock)),
                new SubjectGoal("Java", 300, Instant.now(clock))
        ));
        Instant startedAt = Instant.parse("2026-09-18T13:00:00Z");
        Instant endedAt = Instant.parse("2026-09-18T13:47:00Z");
        when(studyEntryService.getEntriesBetween(weekStart, weekStart.plusDays(6))).thenReturn(List.of(
                new StudyEntry(
                        "clockify-entry",
                        "project-id",
                        "task-id",
                        "Java",
                        "Java",
                        "Java",
                        "DevSuperior",
                        null,
                        "Java",
                        startedAt,
                        endedAt,
                        47,
                        startedAt.atZone(APP_ZONE).toLocalDate(),
                        Instant.now(clock)
                )
        ));

        List<SubjectGoalProgressDto> progress = new SubjectGoalService(
                subjectGoalRepository,
                studyEntryService,
                clock
        ).getProgress();

        assertThat(progress).extracting(SubjectGoalProgressDto::subject)
                .containsExactly("DevSuperior", "Java");
        assertThat(progress).extracting(SubjectGoalProgressDto::studiedMinutes)
                .containsExactly(47L, 47L);
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
                startedAt.atZone(APP_ZONE).toLocalDate(),
                Instant.now()
        );
    }
}
