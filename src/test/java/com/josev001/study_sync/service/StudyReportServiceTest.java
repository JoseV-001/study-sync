package com.josev001.study_sync.service;

import com.josev001.study_sync.dto.AnalyticsInsightDto;
import com.josev001.study_sync.dto.AnalyticsPointDto;
import com.josev001.study_sync.dto.StudyAnalyticsDto;
import com.josev001.study_sync.dto.StudyReportDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudyReportServiceTest {

    @Mock
    private StudyAnalyticsService analyticsService;

    private StudyReportService reportService;

    @BeforeEach
    void setUp() {
        reportService = new StudyReportService(
                analyticsService,
                Clock.fixed(Instant.parse("2026-10-02T15:00:00Z"), ZoneId.of("America/Sao_Paulo"))
        );
    }

    @Test
    void buildsWeeklyComparisonAndShareSummary() {
        LocalDate currentFrom = LocalDate.of(2026, 9, 28);
        LocalDate currentTo = LocalDate.of(2026, 10, 2);
        StudyAnalyticsDto current = analytics(600, 5, "Java");
        StudyAnalyticsDto previous = analytics(400, 4, "SQL");
        when(analyticsService.getAnalytics(currentFrom, currentTo)).thenReturn(current);
        when(analyticsService.getAnalytics(LocalDate.of(2026, 9, 21), LocalDate.of(2026, 9, 25))).thenReturn(previous);

        StudyReportDto report = reportService.getReport("weekly", LocalDate.of(2026, 10, 2));

        assertThat(report.from()).isEqualTo(currentFrom);
        assertThat(report.to()).isEqualTo(currentTo);
        assertThat(report.differenceMinutes()).isEqualTo(200);
        assertThat(report.percentageChange()).isEqualTo(50);
        assertThat(report.shareSummary()).contains("10h 00min", "50%", "Java");
        verify(analyticsService).getAnalytics(LocalDate.of(2026, 9, 21), LocalDate.of(2026, 9, 25));
    }

    @Test
    void currentMonthComparesEqualMonthToDateRanges() {
        when(analyticsService.getAnalytics(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 2))).thenReturn(analytics(60, 1, "Java"));
        when(analyticsService.getAnalytics(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 2))).thenReturn(analytics(30, 1, "SQL"));

        StudyReportDto report = reportService.getReport("monthly", LocalDate.of(2026, 10, 2));

        assertThat(report.to()).isEqualTo(LocalDate.of(2026, 10, 2));
        assertThat(report.previousTo()).isEqualTo(LocalDate.of(2026, 9, 2));
        assertThat(report.percentageChange()).isEqualTo(100);
    }

    @Test
    void monthlyReportUsesCalendarMonthAndSupportsNoPreviousData() {
        StudyAnalyticsDto current = analytics(90, 2, "O\"Reilly, Java");
        when(analyticsService.getAnalytics(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30))).thenReturn(current);
        when(analyticsService.getAnalytics(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31))).thenReturn(analytics(0, 0, "Sem dados"));

        StudyReportDto report = reportService.getReport("monthly", LocalDate.of(2026, 9, 15));
        String csv = reportService.toCsv(report);

        assertThat(report.from()).isEqualTo(LocalDate.of(2026, 9, 1));
        assertThat(report.to()).isEqualTo(LocalDate.of(2026, 9, 30));
        assertThat(report.percentageChange()).isNull();
        assertThat(report.shareSummary()).contains("novo periodo com registros");
        assertThat(csv).startsWith("\uFEFFIndicador,").contains("\"O\"\"Reilly, Java\"");
    }

    @Test
    void rejectsUnknownPeriodType() {
        assertThatThrownBy(() -> reportService.getReport("daily", LocalDate.of(2026, 9, 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("weekly ou monthly");
    }

    private StudyAnalyticsDto analytics(long total, long activeDays, String subject) {
        return new StudyAnalyticsDto(
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 7),
                total,
                total / 7,
                activeDays,
                new AnalyticsInsightDto("Segunda", 60),
                new AnalyticsInsightDto("Domingo", 0),
                new AnalyticsInsightDto("14h - 15h", 60),
                new AnalyticsInsightDto(subject, total),
                List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(new AnalyticsPointDto(subject, total))
        );
    }
}
