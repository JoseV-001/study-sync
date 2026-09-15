package com.josev001.study_sync.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "weekly_study")
public class WeeklyStudy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "week_start", nullable = false, unique = true)
    private LocalDate weekStart;

    @Column(name = "week_end", nullable = false)
    private LocalDate weekEnd;

    @Column(name = "total_minutes", nullable = false)
    private long totalMinutes;

    @Column(name = "notion_time", nullable = false, length = 32)
    private String notionTime;

    @Column(name = "synced_at", nullable = false)
    private Instant syncedAt;

    protected WeeklyStudy() {
    }

    public WeeklyStudy(LocalDate weekStart, LocalDate weekEnd, long totalMinutes,
                       String notionTime, Instant syncedAt) {
        this.weekStart = weekStart;
        this.weekEnd = weekEnd;
        this.totalMinutes = totalMinutes;
        this.notionTime = notionTime;
        this.syncedAt = syncedAt;
    }

    public void update(long totalMinutes, String notionTime, Instant syncedAt) {
        this.totalMinutes = totalMinutes;
        this.notionTime = notionTime;
        this.syncedAt = syncedAt;
    }
}
