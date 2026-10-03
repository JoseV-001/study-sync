package com.josev001.study_sync.dto;

public record BackupSettingsDto(
        boolean enabled,
        String time,
        int retention,
        String directory,
        String lastBackup
) {
}
