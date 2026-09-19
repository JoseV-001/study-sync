package com.josev001.study_sync.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubjectGoalRepository extends JpaRepository<SubjectGoal, Long> {

    Optional<SubjectGoal> findBySubjectIgnoreCase(String subject);

    List<SubjectGoal> findAllByOrderBySubjectAsc();
}
