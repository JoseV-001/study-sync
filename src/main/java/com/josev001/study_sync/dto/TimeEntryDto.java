package com.josev001.study_sync.dto;

public record TimeEntryDto(String id,
                           String description,
                           String userId,
                           String projectId,
                           String taskId,
                           TimeIntervalDto timeInterval) {
}
