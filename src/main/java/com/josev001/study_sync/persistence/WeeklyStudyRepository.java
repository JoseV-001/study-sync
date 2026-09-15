package com.josev001.study_sync.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WeeklyStudyRepository extends JpaRepository<WeeklyStudy, Long> {
    Optional<WeeklyStudy> findByWeekStart(LocalDate weekStart);

    List<WeeklyStudy> findByWeekStartBetweenOrderByWeekStartDesc(
            LocalDate from,
            LocalDate to
    );
}
