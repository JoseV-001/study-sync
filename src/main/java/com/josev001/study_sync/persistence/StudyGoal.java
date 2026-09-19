package com.josev001.study_sync.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "study_goals")
public class StudyGoal {

    @Id
    private Short id;

    @Column(name = "daily_minutes", nullable = false)
    private int dailyMinutes;

    @Column(name = "weekly_minutes", nullable = false)
    private int weeklyMinutes;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected StudyGoal() {
    }

    public StudyGoal(int dailyMinutes, int weeklyMinutes, Instant updatedAt) {
        this.id = 1;
        this.dailyMinutes = dailyMinutes;
        this.weeklyMinutes = weeklyMinutes;
        this.updatedAt = updatedAt;
    }

    public void update(int dailyMinutes, int weeklyMinutes, Instant updatedAt) {
        this.dailyMinutes = dailyMinutes;
        this.weeklyMinutes = weeklyMinutes;
        this.updatedAt = updatedAt;
    }

    public int getDailyMinutes() {
        return dailyMinutes;
    }

    public int getWeeklyMinutes() {
        return weeklyMinutes;
    }
}
