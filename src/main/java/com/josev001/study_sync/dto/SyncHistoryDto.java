package com.josev001.study_sync.dto;

import com.josev001.study_sync.persistence.SyncRunStatus;

import java.time.Instant;
import java.time.LocalDate;

public record SyncHistoryDto(
        Long id,
        LocalDate weekStartDate,
        LocalDate weekEndDate,
        String triggeredBy,
        SyncRunStatus status,
        Long totalMinutes,
        String errorMessage,
        Instant createdAt,
        Instant finishedAt
) {
}
