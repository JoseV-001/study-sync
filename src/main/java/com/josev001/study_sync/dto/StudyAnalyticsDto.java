package com.josev001.study_sync.dto;

import java.time.LocalDate;
import java.util.List;

public record StudyAnalyticsDto(
        LocalDate from,
        LocalDate to,
        long totalMinutes,
        long averageDailyMinutes,
        long activeDays,
        AnalyticsInsightDto mostStudiedDay,
        AnalyticsInsightDto leastStudiedDay,
        AnalyticsInsightDto peakStudyHour,
        AnalyticsInsightDto topSubject,
        List<AnalyticsPointDto> daily,
        List<AnalyticsPointDto> weekly,
        List<AnalyticsPointDto> monthly,
        List<AnalyticsPointDto> weekday,
        List<AnalyticsPointDto> hourly,
        List<AnalyticsPointDto> subjects
) {
}
