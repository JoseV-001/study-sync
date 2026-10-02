package com.josev001.study_sync.dto;

import java.time.Instant;
import java.time.LocalDate;

public record BookDto(
        Long id,
        String title,
        String author,
        int totalPages,
        int currentPage,
        int progressPercentage,
        String status,
        LocalDate targetDate,
        Instant updatedAt
) {
}
