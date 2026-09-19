package com.josev001.study_sync.service;

import com.josev001.study_sync.dto.SubjectGoalProgressDto;
import com.josev001.study_sync.dto.SubjectGoalRequest;
import com.josev001.study_sync.persistence.StudyEntry;
import com.josev001.study_sync.persistence.SubjectGoal;
import com.josev001.study_sync.persistence.SubjectGoalRepository;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class SubjectGoalService {

    private static final ZoneId APP_ZONE = ZoneId.of("America/Sao_Paulo");

    private final SubjectGoalRepository subjectGoalRepository;
    private final StudyEntryService studyEntryService;
    private final Clock clock;

    @Autowired
    public SubjectGoalService(SubjectGoalRepository subjectGoalRepository, StudyEntryService studyEntryService) {
        this(subjectGoalRepository, studyEntryService, Clock.system(APP_ZONE));
    }

    SubjectGoalService(
            SubjectGoalRepository subjectGoalRepository,
            StudyEntryService studyEntryService,
            Clock clock
    ) {
        this.subjectGoalRepository = subjectGoalRepository;
        this.studyEntryService = studyEntryService;
        this.clock = clock;
    }

    public List<SubjectGoalProgressDto> getProgress() {
        LocalDate today = LocalDate.now(clock);
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);
        Map<String, Long> studiedBySubject = getStudiedBySubject(weekStart, weekEnd);

        return subjectGoalRepository.findAllByOrderBySubjectAsc()
                .stream()
                .map(goal -> {
                    long studiedMinutes = studiedBySubject.getOrDefault(normalize(goal.getSubject()), 0L);
                    return new SubjectGoalProgressDto(
                            goal.getId(),
                            goal.getSubject(),
                            goal.getWeeklyMinutes(),
                            studiedMinutes,
                            progressPercentage(studiedMinutes, goal.getWeeklyMinutes()),
                            studiedMinutes >= goal.getWeeklyMinutes()
                    );
                })
                .toList();
    }

    @Transactional
    public List<SubjectGoalProgressDto> save(SubjectGoalRequest request) {
        String subject = request.subject().trim();
        Instant now = Instant.now(clock);
        subjectGoalRepository.findBySubjectIgnoreCase(subject)
                .ifPresentOrElse(
                        goal -> goal.update(request.weeklyMinutes(), now),
                        () -> subjectGoalRepository.save(new SubjectGoal(subject, request.weeklyMinutes(), now))
                );
        return getProgress();
    }

    @Transactional
    public void delete(Long id) {
        subjectGoalRepository.deleteById(id);
    }

    private Map<String, Long> getStudiedBySubject(LocalDate from, LocalDate to) {
        Instant rangeStart = from.atStartOfDay(APP_ZONE).toInstant();
        Instant rangeEnd = to.plusDays(1).atStartOfDay(APP_ZONE).toInstant();
        Map<String, Long> studiedBySubject = new HashMap<>();

        for (StudyEntry entry : studyEntryService.getEntriesBetween(from, to)) {
            Instant startedAt = entry.getStartedAt().isAfter(rangeStart) ? entry.getStartedAt() : rangeStart;
            Instant endedAt = entry.getEndedAt().isBefore(rangeEnd) ? entry.getEndedAt() : rangeEnd;
            if (startedAt.isBefore(endedAt)) {
                long minutes = Duration.between(startedAt, endedAt).toMinutes();
                studiedBySubject.merge(normalize(entry.getSubject()), minutes, Long::sum);
            }
        }
        return studiedBySubject;
    }

    private int progressPercentage(long studiedMinutes, int goalMinutes) {
        return (int) Math.round((studiedMinutes * 100.0) / goalMinutes);
    }

    private String normalize(String subject) {
        return subject == null ? "" : subject.trim().toLowerCase(Locale.ROOT);
    }
}
