package com.josev001.study_sync.dto;

public record IntegrationSettingsDto(
        boolean clockifyConfigured,
        boolean notionConfigured
) {
}
