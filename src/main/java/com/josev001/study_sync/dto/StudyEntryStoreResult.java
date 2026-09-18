package com.josev001.study_sync.dto;

public record StudyEntryStoreResult(
        int createdEntries,
        int updatedEntries,
        int skippedEntries
) {
    public int processedEntries() {
        return createdEntries + updatedEntries;
    }
}
