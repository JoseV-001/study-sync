package com.josev001.study_sync.service;

import com.josev001.study_sync.dto.ClockifyEntryMetadataDto;
import com.josev001.study_sync.dto.StudyEntryStoreResult;
import com.josev001.study_sync.dto.SubjectSuggestionDto;
import com.josev001.study_sync.dto.SubjectClassification;
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
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class StudyEntryService {

    private static final ZoneId APP_ZONE = ZoneId.of("America/Sao_Paulo");

    private final StudyEntryRepository studyEntryRepository;
    private final ClockifyMetadataService clockifyMetadataService;

    public StudyEntryService(
            StudyEntryRepository studyEntryRepository,
            ClockifyMetadataService clockifyMetadataService
    ) {
        this.studyEntryRepository = studyEntryRepository;
        this.clockifyMetadataService = clockifyMetadataService;
    }

    @Transactional
    public int storeEntries(List<TimeEntryDto> entries) {
        return storeEntriesDetailed(entries).processedEntries();
    }

    @Transactional
    public StudyEntryStoreResult storeEntriesDetailed(List<TimeEntryDto> entries) {
        clockifyMetadataService.clearCache();
        Instant syncedAt = Instant.now();
        int createdEntries = 0;
        int updatedEntries = 0;
        int skippedEntries = 0;

        for (TimeEntryDto entry : entries) {
            if (!isCompletedEntry(entry)) {
                skippedEntries++;
                continue;
            }

            Instant startedAt = Instant.parse(entry.timeInterval().start());
            Instant endedAt = Instant.parse(entry.timeInterval().end());
            long durationMinutes = Duration.parse(entry.timeInterval().duration()).toMinutes();
            LocalDate recordedDate = startedAt.atZone(APP_ZONE).toLocalDate();
            ClockifyEntryMetadataDto metadata = clockifyMetadataService.resolve(entry);
            String tagIds = join(entry.tagIds());
            String tagNames = join(metadata.tagNames());
            SubjectClassification classification = classify(entry, metadata);

            Optional<StudyEntry> existingEntry = studyEntryRepository.findById(entry.id());
            if (existingEntry.isPresent()) {
                StudyEntry storedEntry = existingEntry.get();
                storedEntry.update(
                                    entry.projectId(),
                                    entry.taskId(),
                                    metadata.projectName(),
                                    metadata.topicName(),
                                    tagIds,
                                    tagNames,
                                    entry.description(),
                                    classification.name(),
                                    classification.source(),
                                    startedAt,
                                    endedAt,
                                    durationMinutes,
                                    recordedDate,
                                    syncedAt
                );
                updatedEntries++;
            } else {
                studyEntryRepository.save(new StudyEntry(
                                    entry.id(),
                                    entry.projectId(),
                                    entry.taskId(),
                                    metadata.projectName(),
                                    metadata.topicName(),
                                    tagIds,
                                    tagNames,
                                    entry.description(),
                                    classification.name(),
                                    classification.source(),
                                    startedAt,
                                    endedAt,
                                    durationMinutes,
                                    recordedDate,
                                    syncedAt
                ));
                createdEntries++;
            }
        }

        return new StudyEntryStoreResult(createdEntries, updatedEntries, skippedEntries);
    }

    public List<StudyEntry> getEntriesBetween(LocalDate from, LocalDate to) {
        Instant rangeStart = from.atStartOfDay(APP_ZONE).toInstant();
        Instant rangeEnd = to.plusDays(1).atStartOfDay(APP_ZONE).toInstant();
        return studyEntryRepository.findByStartedAtLessThanAndEndedAtGreaterThan(rangeEnd, rangeStart);
    }

    @Transactional
    public int reclassifyStoredEntries() {
        Instant now = Instant.now();
        int changed = 0;
        for (StudyEntry entry : studyEntryRepository.findAll()) {
            SubjectClassification classification = classify(entry.getTopicName(), entry.getTagNames(), entry.getProjectName(), entry.getDescription());
            if (!classification.name().equals(entry.getSubject()) || !classification.source().equals(entry.getSubjectSource())) {
                entry.updateClassification(classification.name(), classification.source(), now);
                changed++;
            }
        }
        return changed;
    }

    @Transactional(readOnly = true)
    public List<SubjectSuggestionDto> getKnownSubjects() {
        Map<String, SubjectSuggestionDto> suggestions = new LinkedHashMap<>();
        addSuggestions(suggestions, studyEntryRepository.findDistinctTopicSubjects(), "Topico");
        addSuggestions(suggestions, studyEntryRepository.findDistinctTagSubjects(), "Tag");
        addSuggestions(suggestions, studyEntryRepository.findDistinctDescriptionSubjects(), "Descricao");
        addSuggestions(suggestions, studyEntryRepository.findDistinctProjectSubjects(), "Projeto");
        return List.copyOf(suggestions.values());
    }

    private void addSuggestions(
            Map<String, SubjectSuggestionDto> suggestions,
            List<String> subjects,
            String category
    ) {
        subjects.stream()
                .filter(this::hasText)
                .map(this::limit)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .forEach(subject -> suggestions.putIfAbsent(
                        subject.toLowerCase(Locale.ROOT),
                        new SubjectSuggestionDto(subject, category)
                ));
    }

    private boolean isCompletedEntry(TimeEntryDto entry) {
        return entry != null
                && entry.id() != null
                && entry.timeInterval() != null
                && entry.timeInterval().start() != null
                && entry.timeInterval().end() != null
                && entry.timeInterval().duration() != null;
    }

    private SubjectClassification classify(TimeEntryDto entry, ClockifyEntryMetadataDto metadata) {
        return classify(metadata.topicName(), join(metadata.tagNames()), metadata.projectName(), entry.description());
    }

    private SubjectClassification classify(String topic, String tags, String project, String description) {
        if (hasText(topic)) return new SubjectClassification(limit(topic), "Topico");
        String firstTag = firstTag(tags);
        if (hasText(firstTag)) return new SubjectClassification(limit(firstTag), "Tag");
        if (hasText(project)) return new SubjectClassification(limit(project), "Projeto");
        if (isUsefulDescription(description)) return new SubjectClassification(limit(description), "Descricao");
        return new SubjectClassification("Sem materia", "Sem classificacao");
    }

    private String firstTag(String tags) {
        if (!hasText(tags)) return null;
        return tags.split(",")[0].trim();
    }

    private boolean isUsefulDescription(String description) {
        if (!hasText(description)) return false;
        String normalized = description.trim().toLowerCase(Locale.ROOT);
        return !List.of("what are you working on?", "add task", "add tag", "time entry").contains(normalized);
    }

    private String join(List<String> values) {
        return values == null ? null : String.join(", ", values);
    }

    private String limit(String value) {
        String trimmed = value.trim();
        return trimmed.length() <= 256 ? trimmed : trimmed.substring(0, 256);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
