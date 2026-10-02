package com.josev001.study_sync.dto;

public record AutomaticSyncSettingsDto(
        boolean enabled,
        boolean syncOnStartup,
        String dayOfWeek,
        String time,
        String zone
) {
}
