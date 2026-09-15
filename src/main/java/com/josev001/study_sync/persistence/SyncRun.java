package com.josev001.study_sync.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "sync_runs")
public class SyncRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "week_start", nullable = false)
    private LocalDate weekStart;

    @Column(name = "triggered_by", nullable = false, length = 64)
    private String triggeredBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private SyncRunStatus status;

    @Column(name = "total_minutes")
    private Long totalMinutes;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    protected SyncRun() {
    }

    public SyncRun(LocalDate weekStart, String triggeredBy, Instant createdAt) {
        this.weekStart = weekStart;
        this.triggeredBy = triggeredBy;
        this.status = SyncRunStatus.RUNNING;
        this.createdAt = createdAt;
    }

    public void markSuccess(long totalMinutes, Instant finishedAt) {
        this.status = SyncRunStatus.SUCCESS;
        this.totalMinutes = totalMinutes;
        this.finishedAt = finishedAt;
    }

    public void markFailed(String errorMessage, Instant finishedAt) {
        this.status = SyncRunStatus.FAILED;
        this.errorMessage = errorMessage;
        this.finishedAt = finishedAt;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getWeekStart() {
        return weekStart;
    }

    public String getTriggeredBy() {
        return triggeredBy;
    }

    public SyncRunStatus getStatus() {
        return status;
    }

    public Long getTotalMinutes() {
        return totalMinutes;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }
}
