package com.josev001.study_sync.dto;

import java.time.LocalDate;

public record StudyGoalProgressDto(
        LocalDate today,
        LocalDate weekStart,
        LocalDate weekEnd,
        int dailyGoalMinutes,
        int weeklyGoalMinutes,
        long todayMinutes,
        long weekMinutes,
        int dailyProgressPercentage,
        int weeklyProgressPercentage,
        boolean dailyGoalReached,
        boolean weeklyGoalReached
) {
}
