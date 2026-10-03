package com.josev001.study_sync.service;

import com.josev001.study_sync.dto.AnalyticsPointDto;
import com.josev001.study_sync.dto.StudyAnalyticsDto;
import com.josev001.study_sync.dto.StudyReportDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;

@Service
public class StudyReportService {

    private static final ZoneId APP_ZONE = ZoneId.of("America/Sao_Paulo");
    private final StudyAnalyticsService analyticsService;
    private final Clock clock;

    @Autowired
    public StudyReportService(StudyAnalyticsService analyticsService) {
        this(analyticsService, Clock.system(APP_ZONE));
    }

    StudyReportService(StudyAnalyticsService analyticsService, Clock clock) {
        this.analyticsService = analyticsService;
        this.clock = clock;
    }

    public StudyReportDto getReport(String periodType, LocalDate selectedDate) {
        String normalizedType = periodType == null ? "weekly" : periodType.trim().toLowerCase(Locale.ROOT);
        LocalDate today = LocalDate.now(clock);
        LocalDate anchor = selectedDate == null ? today : selectedDate;
        LocalDate from;
        LocalDate to;
        LocalDate previousFrom;
        LocalDate previousTo;
        String label;

        if ("weekly".equals(normalizedType)) {
            from = anchor.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            to = from.plusDays(6);
            previousFrom = from.minusWeeks(1);
            if (!from.isAfter(today) && to.isAfter(today)) {
                to = today;
            }
            previousTo = previousFrom.plusDays(ChronoUnit.DAYS.between(from, to));
            label = "Semana de " + from + " a " + to;
        } else if ("monthly".equals(normalizedType)) {
            YearMonth month = YearMonth.from(anchor);
            from = month.atDay(1);
            to = month.atEndOfMonth();
            previousFrom = month.minusMonths(1).atDay(1);
            previousTo = month.minusMonths(1).atEndOfMonth();
            if (!from.isAfter(today) && to.isAfter(today)) {
                to = today;
                previousTo = previousFrom.plusDays(ChronoUnit.DAYS.between(from, to));
            }
            label = month.getMonth().getDisplayName(java.time.format.TextStyle.FULL, Locale.forLanguageTag("pt-BR"))
                    + " de " + month.getYear();
            label = Character.toUpperCase(label.charAt(0)) + label.substring(1);
        } else {
            throw new IllegalArgumentException("O periodo deve ser weekly ou monthly.");
        }

        StudyAnalyticsDto current = analyticsService.getAnalytics(from, to);
        StudyAnalyticsDto previous = analyticsService.getAnalytics(previousFrom, previousTo);
        long difference = current.totalMinutes() - previous.totalMinutes();
        Integer percentage;
        if (previous.totalMinutes() == 0) {
            percentage = null;
        } else {
            percentage = (int) Math.round(difference * 100.0 / previous.totalMinutes());
        }

        return new StudyReportDto(
                normalizedType,
                label,
                from,
                to,
                current,
                previousFrom,
                previousTo,
                previous,
                difference,
                percentage,
                shareSummary(normalizedType, current, previous, difference, percentage)
        );
    }

    public String toCsv(StudyReportDto report) {
        StringBuilder csv = new StringBuilder("\uFEFF");
        csv.append("Indicador,Periodo atual,Periodo anterior,Variacao\n")
                .append("Periodo,").append(csv(report.from() + " a " + report.to())).append(',')
                .append(csv(report.previousFrom() + " a " + report.previousTo())).append(",\n")
                .append("Total estudado,").append(csv(formatMinutes(report.current().totalMinutes()))).append(',')
                .append(csv(formatMinutes(report.previous().totalMinutes()))).append(',')
                .append(csv(formatDifference(report.differenceMinutes()))).append('\n')
                .append("Media diaria,").append(csv(formatMinutes(report.current().averageDailyMinutes()))).append(',')
                .append(csv(formatMinutes(report.previous().averageDailyMinutes()))).append(",\n")
                .append("Dias ativos,").append(report.current().activeDays()).append(',')
                .append(report.previous().activeDays()).append(",\n")
                .append("Materia principal,").append(csv(label(report.current().topSubject()))).append(',')
                .append(csv(label(report.previous().topSubject()))).append(",\n")
                .append("Mais estudado na semana,").append(csv(label(report.current().mostStudiedDay()))).append(',')
                .append(csv(label(report.previous().mostStudiedDay()))).append(",\n")
                .append("Horario de maior foco,").append(csv(label(report.current().peakStudyHour()))).append(',')
                .append(csv(label(report.previous().peakStudyHour()))).append(",\n\n")
                .append("Materia,Minutos no periodo atual\n");
        for (AnalyticsPointDto subject : report.current().subjects()) {
            if (subject.totalMinutes() > 0) {
                csv.append(csv(subject.label())).append(',').append(subject.totalMinutes()).append('\n');
            }
        }
        return csv.toString();
    }

    private String shareSummary(String periodType, StudyAnalyticsDto current, StudyAnalyticsDto previous,
                                long difference, Integer percentage) {
        String period = "weekly".equals(periodType) ? "Neste periodo semanal" : "Neste periodo mensal";
        String direction = difference > 0 ? "a mais" : difference < 0 ? "a menos" : "o mesmo tempo";
        String comparison = previous.totalMinutes() == 0
                ? (current.totalMinutes() > 0 ? "um novo periodo com registros" : "sem registros para comparar no periodo anterior")
                : direction + " que no periodo anterior" + (percentage == null ? "" : " (" + Math.abs(percentage) + "% de variacao)");
        return period + ", estudei " + formatMinutes(current.totalMinutes()) + " em " + current.activeDays()
                + (current.activeDays() == 1 ? " dia ativo" : " dias ativos") + ". Isso representa " + comparison
                + ". Minha materia principal foi " + label(current.topSubject()) + ", e meu dia mais ativo foi "
                + label(current.mostStudiedDay()) + ".";
    }

    private String label(com.josev001.study_sync.dto.AnalyticsInsightDto insight) {
        return insight == null || insight.label() == null || "Sem dados".equals(insight.label()) ? "Sem registros" : insight.label();
    }

    private String formatMinutes(long minutes) {
        return (minutes / 60) + "h " + String.format(Locale.ROOT, "%02dmin", minutes % 60);
    }

    private String formatDifference(long minutes) {
        if (minutes == 0) return "Sem variacao";
        return (minutes > 0 ? "+" : "-") + formatMinutes(Math.abs(minutes));
    }

    private String csv(String value) {
        String safe = value == null ? "" : value;
        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }
}
