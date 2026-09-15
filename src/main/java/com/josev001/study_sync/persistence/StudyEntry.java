package com.josev001.study_sync.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "study_entries")
public class StudyEntry {

    @Id
    @Column(name = "clockify_entry_id", length = 64)
    private String clockifyEntryId;

    @Column(name = "project_id", length = 64)
    private String projectId;

    @Column(name = "task_id", length = 64)
    private String taskId;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, length = 256)
    private String subject;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "ended_at", nullable = false)
    private Instant endedAt;

    @Column(name = "duration_minutes", nullable = false)
    private long durationMinutes;

    @Column(name = "recorded_date", nullable = false)
    private LocalDate recordedDate;

    @Column(name = "synced_at", nullable = false)
    private Instant syncedAt;

    protected StudyEntry() {
    }

    public StudyEntry(
            String clockifyEntryId,
            String projectId,
            String taskId,
            String description,
            String subject,
            Instant startedAt,
            Instant endedAt,
            long durationMinutes,
            LocalDate recordedDate,
            Instant syncedAt
    ) {
        this.clockifyEntryId = clockifyEntryId;
        this.projectId = projectId;
        this.taskId = taskId;
        this.description = description;
        this.subject = subject;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.durationMinutes = durationMinutes;
        this.recordedDate = recordedDate;
        this.syncedAt = syncedAt;
    }

    public void update(
            String projectId,
            String taskId,
            String description,
            String subject,
            Instant startedAt,
            Instant endedAt,
            long durationMinutes,
            LocalDate recordedDate,
            Instant syncedAt
    ) {
        this.projectId = projectId;
        this.taskId = taskId;
        this.description = description;
        this.subject = subject;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.durationMinutes = durationMinutes;
        this.recordedDate = recordedDate;
        this.syncedAt = syncedAt;
    }

    public String getSubject() {
        return subject;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getEndedAt() {
        return endedAt;
    }
}
