package com.josev001.study_sync.dto;

import java.time.LocalDate;

public record SyncResultDto(
        LocalDate weekStartDate,
        LocalDate weekEndDate,
        String syncedTime,
        boolean notionUpdated
) {
}
