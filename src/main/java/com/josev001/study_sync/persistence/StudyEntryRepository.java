package com.josev001.study_sync.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface StudyEntryRepository extends JpaRepository<StudyEntry, String> {

    List<StudyEntry> findAllByOrderByStartedAtAsc();

    List<StudyEntry> findByStartedAtLessThanAndEndedAtGreaterThan(
            Instant rangeEnd,
            Instant rangeStart
    );

    @Query("""
            select distinct entry.subject
            from StudyEntry entry
            where entry.subject is not null and trim(entry.subject) <> ''
            """)
    List<String> findDistinctSubjects();
}
