package com.josev001.study_sync.service;

import com.josev001.study_sync.dto.AnalyticsInsightDto;
import com.josev001.study_sync.dto.AnalyticsPointDto;
import com.josev001.study_sync.dto.StudyAnalyticsDto;
import com.josev001.study_sync.persistence.StudyEntry;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class StudyAnalyticsService {

    private static final ZoneId APP_ZONE = ZoneId.of("America/Sao_Paulo");

    private final StudyEntryService studyEntryService;

    public StudyAnalyticsService(StudyEntryService studyEntryService) {
        this.studyEntryService = studyEntryService;
    }

    public StudyAnalyticsDto getAnalytics(LocalDate from, LocalDate to) {
        validateRange(from, to);
        Map<LocalDate, Long> dailySeconds = createDailySeries(from, to);
        Map<DayOfWeek, Long> weekdaySeconds = createWeekdaySeries();
        Map<Integer, Long> hourlySeconds = createHourlySeries();
        Map<LocalDate, Long> weeklySeconds = new TreeMap<>();
        Map<YearMonth, Long> monthlySeconds = new TreeMap<>();
        Map<String, Long> subjectSeconds = new TreeMap<>();

        Instant rangeStart = from.atStartOfDay(APP_ZONE).toInstant();
        Instant rangeEnd = to.plusDays(1).atStartOfDay(APP_ZONE).toInstant();
        for (StudyEntry entry : studyEntryService.getEntriesBetween(from, to)) {
            distributeEntry(
                    entry,
                    rangeStart,
                    rangeEnd,
                    dailySeconds,
                    weekdaySeconds,
                    hourlySeconds,
                    weeklySeconds,
                    monthlySeconds,
                    subjectSeconds
            );
        }

        long totalSeconds = dailySeconds.values().stream().mapToLong(Long::longValue).sum();
        long activeDays = dailySeconds.values().stream().filter(seconds -> seconds > 0).count();
        long daysInPeriod = Duration.between(rangeStart, rangeEnd).toDays();

        return new StudyAnalyticsDto(
                from,
                to,
                toMinutes(totalSeconds),
                daysInPeriod == 0 ? 0 : toMinutes(totalSeconds / daysInPeriod),
                activeDays,
                insight(weekdaySeconds, this::weekdayLabel, false),
                insight(weekdaySeconds, this::weekdayLabel, true),
                insight(hourlySeconds, this::hourLabel, false),
                insight(subjectSeconds, label -> label, false),
                points(dailySeconds, LocalDate::toString),
                points(weeklySeconds, LocalDate::toString),
                points(monthlySeconds, YearMonth::toString),
                points(weekdaySeconds, this::weekdayLabel),
                points(hourlySeconds, this::hourLabel),
                points(subjectSeconds, label -> label)
        );
    }

    private void distributeEntry(
            StudyEntry entry,
            Instant rangeStart,
            Instant rangeEnd,
            Map<LocalDate, Long> dailySeconds,
            Map<DayOfWeek, Long> weekdaySeconds,
            Map<Integer, Long> hourlySeconds,
            Map<LocalDate, Long> weeklySeconds,
            Map<YearMonth, Long> monthlySeconds,
            Map<String, Long> subjectSeconds
    ) {
        Instant cursor = entry.getStartedAt().isAfter(rangeStart) ? entry.getStartedAt() : rangeStart;
        Instant end = entry.getEndedAt().isBefore(rangeEnd) ? entry.getEndedAt() : rangeEnd;

        while (cursor.isBefore(end)) {
            var zonedCursor = cursor.atZone(APP_ZONE);
            Instant nextHour = zonedCursor.truncatedTo(java.time.temporal.ChronoUnit.HOURS)
                    .plusHours(1)
                    .toInstant();
            Instant nextDay = zonedCursor.toLocalDate().plusDays(1).atStartOfDay(APP_ZONE).toInstant();
            Instant segmentEnd = min(end, min(nextHour, nextDay));
            long seconds = Duration.between(cursor, segmentEnd).getSeconds();
            LocalDate day = zonedCursor.toLocalDate();

            add(dailySeconds, day, seconds);
            add(weekdaySeconds, day.getDayOfWeek(), seconds);
            add(hourlySeconds, zonedCursor.getHour(), seconds);
            add(weeklySeconds, day.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)), seconds);
            add(monthlySeconds, YearMonth.from(day), seconds);
            add(subjectSeconds, entry.getSubject(), seconds);
            cursor = segmentEnd;
        }
    }

    private <K> AnalyticsInsightDto insight(
            Map<K, Long> series,
            java.util.function.Function<K, String> labelFormatter,
            boolean smallest
    ) {
        Comparator<Map.Entry<K, Long>> comparator = Comparator.comparingLong(entry -> entry.getValue());
        var entries = series.entrySet().stream();
        return (smallest ? entries.min(comparator) : entries.max(comparator))
                .map(entry -> new AnalyticsInsightDto(labelFormatter.apply(entry.getKey()), toMinutes(entry.getValue())))
                .orElse(new AnalyticsInsightDto("Sem dados", 0));
    }

    private <K> List<AnalyticsPointDto> points(
            Map<K, Long> series,
            java.util.function.Function<K, String> labelFormatter
    ) {
        return series.entrySet().stream()
                .map(entry -> new AnalyticsPointDto(labelFormatter.apply(entry.getKey()), toMinutes(entry.getValue())))
                .toList();
    }

    private Map<LocalDate, Long> createDailySeries(LocalDate from, LocalDate to) {
        Map<LocalDate, Long> series = new TreeMap<>();
        for (LocalDate day = from; !day.isAfter(to); day = day.plusDays(1)) {
            series.put(day, 0L);
        }
        return series;
    }

    private Map<DayOfWeek, Long> createWeekdaySeries() {
        Map<DayOfWeek, Long> series = new LinkedHashMap<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            series.put(day, 0L);
        }
        return series;
    }

    private Map<Integer, Long> createHourlySeries() {
        Map<Integer, Long> series = new LinkedHashMap<>();
        for (int hour = 0; hour < 24; hour++) {
            series.put(hour, 0L);
        }
        return series;
    }

    private <K> void add(Map<K, Long> series, K key, long value) {
        series.merge(key, value, Long::sum);
    }

    private Instant min(Instant first, Instant second) {
        return first.isBefore(second) ? first : second;
    }

    private long toMinutes(long seconds) {
        return Math.round(seconds / 60.0);
    }

    private String weekdayLabel(DayOfWeek day) {
        return switch (day) {
            case MONDAY -> "Segunda";
            case TUESDAY -> "Terca";
            case WEDNESDAY -> "Quarta";
            case THURSDAY -> "Quinta";
            case FRIDAY -> "Sexta";
            case SATURDAY -> "Sabado";
            case SUNDAY -> "Domingo";
        };
    }

    private String hourLabel(Integer hour) {
        return String.format("%02dh - %02dh", hour, (hour + 1) % 24);
    }

    private void validateRange(LocalDate from, LocalDate to) {
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("The end date must not be before the start date");
        }
        if (Duration.between(from.atStartOfDay(APP_ZONE), to.plusDays(1).atStartOfDay(APP_ZONE)).toDays() > 366) {
            throw new IllegalArgumentException("The selected period must be up to 366 days");
        }
    }
}
