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
            where entry.topicName is not null
              and trim(entry.topicName) <> ''
              and entry.subject = entry.topicName
            """)
    List<String> findDistinctTopicSubjects();

    @Query("""
            select distinct entry.subject
            from StudyEntry entry
            where entry.topicName is null
              and entry.tagNames is not null
              and trim(entry.tagNames) <> ''
              and entry.subject is not null
              and trim(entry.subject) <> ''
            """)
    List<String> findDistinctTagSubjects();

    @Query("""
            select distinct entry.subject
            from StudyEntry entry
            where entry.projectName is not null
              and trim(entry.projectName) <> ''
              and entry.subject = entry.projectName
            """)
    List<String> findDistinctProjectSubjects();
}
