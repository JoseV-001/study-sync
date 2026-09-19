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

    @Column(name = "project_name", length = 512)
    private String projectName;

    @Column(name = "topic_name", length = 512)
    private String topicName;

    @Column(name = "tag_ids", length = 2000)
    private String tagIds;

    @Column(name = "tag_names", length = 2000)
    private String tagNames;

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
        this.clockifyEntryId = clockifyEntryId;
        this.projectId = projectId;
        this.taskId = taskId;
        this.projectName = projectName;
        this.topicName = topicName;
        this.tagIds = tagIds;
        this.tagNames = tagNames;
        this.description = description;
        this.subject = subject;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.durationMinutes = durationMinutes;
        this.recordedDate = recordedDate;
        this.syncedAt = syncedAt;
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
        this(
                clockifyEntryId,
                projectId,
                taskId,
                null,
                null,
                null,
                null,
                description,
                subject,
                startedAt,
                endedAt,
                durationMinutes,
                recordedDate,
                syncedAt
        );
    }

    public void update(
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
        this.projectId = projectId;
        this.taskId = taskId;
        this.projectName = projectName;
        this.topicName = topicName;
        this.tagIds = tagIds;
        this.tagNames = tagNames;
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

    public String getClockifyEntryId() {
        return clockifyEntryId;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getTopicName() {
        return topicName;
    }

    public String getTagNames() {
        return tagNames;
    }

    public String getTagIds() {
        return tagIds;
    }

    public String getDescription() {
        return description;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getEndedAt() {
        return endedAt;
    }

    public long getDurationMinutes() {
        return durationMinutes;
    }

    public LocalDate getRecordedDate() {
        return recordedDate;
    }

    public Instant getSyncedAt() {
        return syncedAt;
    }
}
