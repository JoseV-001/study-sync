package com.josev001.study_sync.service;

import com.josev001.study_sync.dto.StudyGoalDto;
import com.josev001.study_sync.dto.StudyGoalProgressDto;
import com.josev001.study_sync.dto.StudyGoalRequest;
import com.josev001.study_sync.persistence.StudyGoal;
import com.josev001.study_sync.persistence.StudyGoalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;

@Service
public class StudyGoalService {

    private static final short GOAL_ID = 1;
    private static final ZoneId APP_ZONE = ZoneId.of("America/Sao_Paulo");

    private final StudyGoalRepository studyGoalRepository;
    private final StudyEntryService studyEntryService;
    private final Clock clock;

    @Autowired
    public StudyGoalService(StudyGoalRepository studyGoalRepository, StudyEntryService studyEntryService) {
        this(studyGoalRepository, studyEntryService, Clock.system(APP_ZONE));
    }

    StudyGoalService(
            StudyGoalRepository studyGoalRepository,
            StudyEntryService studyEntryService,
            Clock clock
    ) {
        this.studyGoalRepository = studyGoalRepository;
        this.studyEntryService = studyEntryService;
        this.clock = clock;
    }

    public StudyGoalDto getGoals() {
        return studyGoalRepository.findById(GOAL_ID)
                .map(goal -> new StudyGoalDto(goal.getDailyMinutes(), goal.getWeeklyMinutes()))
                .orElse(new StudyGoalDto(0, 0));
    }

    @Transactional
    public StudyGoalDto saveGoals(StudyGoalRequest request) {
        Instant now = Instant.now(clock);
        studyGoalRepository.findById(GOAL_ID)
                .ifPresentOrElse(
                        goal -> goal.update(request.dailyMinutes(), request.weeklyMinutes(), now),
                        () -> studyGoalRepository.save(new StudyGoal(request.dailyMinutes(), request.weeklyMinutes(), now))
                );
        return new StudyGoalDto(request.dailyMinutes(), request.weeklyMinutes());
    }

    public StudyGoalProgressDto getProgress() {
        LocalDate today = LocalDate.now(clock);
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);
        StudyGoalDto goals = getGoals();
        long todayMinutes = sumMinutes(today, today);
        long weekMinutes = sumMinutes(weekStart, weekEnd);

        return new StudyGoalProgressDto(
                today,
                weekStart,
                weekEnd,
                goals.dailyMinutes(),
                goals.weeklyMinutes(),
                todayMinutes,
                weekMinutes,
                progressPercentage(todayMinutes, goals.dailyMinutes()),
                progressPercentage(weekMinutes, goals.weeklyMinutes()),
                goals.dailyMinutes() > 0 && todayMinutes >= goals.dailyMinutes(),
                goals.weeklyMinutes() > 0 && weekMinutes >= goals.weeklyMinutes()
        );
    }

    private int progressPercentage(long currentMinutes, int goalMinutes) {
        if (goalMinutes <= 0) {
            return 0;
        }
        return (int) Math.round((currentMinutes * 100.0) / goalMinutes);
    }

    private long sumMinutes(LocalDate from, LocalDate to) {
        Instant rangeStart = from.atStartOfDay(APP_ZONE).toInstant();
        Instant rangeEnd = to.plusDays(1).atStartOfDay(APP_ZONE).toInstant();
        return studyEntryService.getEntriesBetween(from, to).stream()
                .mapToLong(entry -> {
                    Instant startedAt = entry.getStartedAt().isAfter(rangeStart) ? entry.getStartedAt() : rangeStart;
                    Instant endedAt = entry.getEndedAt().isBefore(rangeEnd) ? entry.getEndedAt() : rangeEnd;
                    return startedAt.isBefore(endedAt) ? Duration.between(startedAt, endedAt).toMinutes() : 0;
                })
                .sum();
    }
}
