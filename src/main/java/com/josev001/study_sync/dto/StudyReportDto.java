package com.josev001.study_sync.dto;

import java.time.LocalDate;

public record StudyReportDto(
        String periodType,
        String periodLabel,
        LocalDate from,
        LocalDate to,
        StudyAnalyticsDto current,
        LocalDate previousFrom,
        LocalDate previousTo,
        StudyAnalyticsDto previous,
        long differenceMinutes,
        Integer percentageChange,
        String shareSummary
) {
}
