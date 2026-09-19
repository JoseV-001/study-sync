package com.josev001.study_sync.dto;

public record SubjectGoalProgressDto(
        Long id,
        String subject,
        int weeklyGoalMinutes,
        long studiedMinutes,
        int progressPercentage,
        boolean goalReached
) {
}
