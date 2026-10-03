package com.josev001.study_sync.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record StudyBackupDto(
        String format,
        Instant exportedAt,
        List<StudyEntryBackupDto> studyEntries,
        List<WeeklyStudyBackupDto> weeklyStudies,
        List<SyncRunBackupDto> syncRuns,
        StudyGoalBackupDto studyGoal,
        List<SubjectGoalBackupDto> subjectGoals,
        List<BookBackupDto> books
) {

    public static final String FORMAT = "study-sync-backup-v2";
    public static final String LEGACY_FORMAT = "study-sync-backup-v1";

    public record StudyEntryBackupDto(
            String clockifyEntryId,
            String projectId,
            String taskId,
            String projectName,
            String topicName,
            String tagIds,
            String tagNames,
            String description,
            String subject,
            Instant startedAt,
            Instant endedAt,
            long durationMinutes,
            LocalDate recordedDate,
            Instant syncedAt
    ) {
    }

    public record WeeklyStudyBackupDto(
            LocalDate weekStart,
            LocalDate weekEnd,
            long totalMinutes,
            String notionTime,
            Instant syncedAt
    ) {
    }

    public record SyncRunBackupDto(
            LocalDate weekStart,
            String triggeredBy,
            String status,
            Long totalMinutes,
            String errorMessage,
            Instant createdAt,
            Instant finishedAt
    ) {
    }

    public record StudyGoalBackupDto(int dailyMinutes, int weeklyMinutes) {
    }

    public record SubjectGoalBackupDto(String subject, int weeklyMinutes) {
    }

    public record BookBackupDto(
            String title,
            String author,
            int totalPages,
            int currentPage,
            int weeklyPageGoal,
            String status,
            LocalDate targetDate,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}
