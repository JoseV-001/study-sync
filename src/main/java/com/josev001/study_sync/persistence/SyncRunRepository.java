package com.josev001.study_sync.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SyncRunRepository extends JpaRepository<SyncRun, Long> {
    List<SyncRun> findTop50ByOrderByCreatedAtDesc();
}
