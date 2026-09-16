package com.josev001.study_sync.dto;

import java.util.List;

public record ClockifyEntryMetadataDto(
        String projectName,
        String topicName,
        List<String> tagNames
) {
}
