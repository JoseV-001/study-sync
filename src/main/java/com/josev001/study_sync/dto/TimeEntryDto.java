package com.josev001.study_sync.dto;

import java.util.List;

public record TimeEntryDto(String id,
                           String description,
                           String userId,
                           String projectId,
                           String taskId,
                           List<String> tagIds,
                           TimeIntervalDto timeInterval) {
}
