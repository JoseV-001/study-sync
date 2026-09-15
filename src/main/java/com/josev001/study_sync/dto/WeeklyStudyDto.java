package com.josev001.study_sync.dto;

import java.time.Instant;
import java.time.LocalDate;

public record WeeklyStudyDto(
        LocalDate weekStartDate,
        LocalDate weekEndDate,
        long totalMinutes,
        String notionTime,
        Instant syncedAt
) {
}
