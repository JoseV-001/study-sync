package com.josev001.study_sync.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface StudyEntryRepository extends JpaRepository<StudyEntry, String> {

    List<StudyEntry> findByStartedAtLessThanAndEndedAtGreaterThan(
            Instant rangeEnd,
            Instant rangeStart
    );
}
