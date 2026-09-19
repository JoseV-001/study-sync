package com.josev001.study_sync.dto;

public record IntegrationSettingsRequest(
        String clockifyApiKey,
        String notionApiKey
) {
}
