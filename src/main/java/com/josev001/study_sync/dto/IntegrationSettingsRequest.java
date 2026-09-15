package com.josev001.study_sync.dto;

import jakarta.validation.constraints.NotBlank;

public record IntegrationSettingsRequest(
        @NotBlank(message = "Clockify API key is required")
        String clockifyApiKey,
        String notionApiKey
) {
}
