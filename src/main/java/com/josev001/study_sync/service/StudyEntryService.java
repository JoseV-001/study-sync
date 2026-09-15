package com.josev001.study_sync.service;

import com.josev001.study_sync.dto.TimeEntryDto;
import com.josev001.study_sync.persistence.StudyEntry;
import com.josev001.study_sync.persistence.StudyEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class StudyEntryService {

    private static final ZoneId APP_ZONE = ZoneId.of("America/Sao_Paulo");

    private final StudyEntryRepository studyEntryRepository;

    public StudyEntryService(StudyEntryRepository studyEntryRepository) {
        this.studyEntryRepository = studyEntryRepository;
    }

    @Transactional
    public int storeEntries(List<TimeEntryDto> entries) {
        Instant syncedAt = Instant.now();
        int storedEntries = 0;

        for (TimeEntryDto entry : entries) {
            if (!isCompletedEntry(entry)) {
                continue;
            }

            Instant startedAt = Instant.parse(entry.timeInterval().start());
            Instant endedAt = Instant.parse(entry.timeInterval().end());
            long durationMinutes = Duration.parse(entry.timeInterval().duration()).toMinutes();
            LocalDate recordedDate = startedAt.atZone(APP_ZONE).toLocalDate();
            String subject = getSubject(entry);

            studyEntryRepository.findById(entry.id())
                    .ifPresentOrElse(
                            storedEntry -> storedEntry.update(
                                    entry.projectId(),
                                    entry.taskId(),
                                    entry.description(),
                                    subject,
                                    startedAt,
                                    endedAt,
                                    durationMinutes,
                                    recordedDate,
                                    syncedAt
                            ),
                            () -> studyEntryRepository.save(new StudyEntry(
                                    entry.id(),
                                    entry.projectId(),
                                    entry.taskId(),
                                    entry.description(),
                                    subject,
                                    startedAt,
                                    endedAt,
                                    durationMinutes,
                                    recordedDate,
                                    syncedAt
                            ))
                    );
            storedEntries++;
        }

        return storedEntries;
    }

    public List<StudyEntry> getEntriesBetween(LocalDate from, LocalDate to) {
        Instant rangeStart = from.atStartOfDay(APP_ZONE).toInstant();
        Instant rangeEnd = to.plusDays(1).atStartOfDay(APP_ZONE).toInstant();
        return studyEntryRepository.findByStartedAtLessThanAndEndedAtGreaterThan(rangeEnd, rangeStart);
    }

    private boolean isCompletedEntry(TimeEntryDto entry) {
        return entry != null
                && entry.id() != null
                && entry.timeInterval() != null
                && entry.timeInterval().start() != null
                && entry.timeInterval().end() != null
                && entry.timeInterval().duration() != null;
    }

    private String getSubject(TimeEntryDto entry) {
        if (entry.description() != null && !entry.description().isBlank()) {
            return entry.description().trim();
        }
        if (entry.projectId() != null && !entry.projectId().isBlank()) {
            return "Projeto " + entry.projectId();
        }
        return "Sem materia";
    }
}
