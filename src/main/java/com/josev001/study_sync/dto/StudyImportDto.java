package com.josev001.study_sync.dto;

import java.time.LocalDate;

public record StudyImportDto(
        LocalDate from,
        LocalDate to,
        int importedEntries,
        long totalMinutes
) {
}
