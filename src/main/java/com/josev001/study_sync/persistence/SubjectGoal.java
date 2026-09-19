package com.josev001.study_sync.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(
        name = "subject_goals",
        uniqueConstraints = @UniqueConstraint(name = "uk_subject_goals_subject", columnNames = "subject")
)
public class SubjectGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 256)
    private String subject;

    @Column(name = "weekly_minutes", nullable = false)
    private int weeklyMinutes;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected SubjectGoal() {
    }

    public SubjectGoal(String subject, int weeklyMinutes, Instant updatedAt) {
        this.subject = subject;
        this.weeklyMinutes = weeklyMinutes;
        this.updatedAt = updatedAt;
    }

    public void update(int weeklyMinutes, Instant updatedAt) {
        this.weeklyMinutes = weeklyMinutes;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getSubject() {
        return subject;
    }

    public int getWeeklyMinutes() {
        return weeklyMinutes;
    }
}
