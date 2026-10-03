package com.josev001.study_sync.service;

import com.josev001.study_sync.dto.BackupSettingsDto;
import com.josev001.study_sync.dto.BackupSettingsRequest;
import com.josev001.study_sync.persistence.AppSetting;
import com.josev001.study_sync.persistence.AppSettingRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalTime;

@Service
public class BackupSettingsService {

    private static final String ENABLED = "backup.enabled";
    private static final String TIME = "backup.time";
    private static final String RETENTION = "backup.retention";
    private static final String LAST_RUN = "backup.last-run";
    private static final String LAST_BACKUP = "backup.last-file";

    private final AppSettingRepository repository;
    private final Path backupDirectory;

    public BackupSettingsService(
            AppSettingRepository repository,
            @Value("${study-sync.data-dir:${user.home}/.study-sync}") String dataDirectory
    ) {
        this.repository = repository;
        this.backupDirectory = Path.of(dataDirectory).resolve("backups");
    }

    public BackupSettingsDto getSettings() {
        return new BackupSettingsDto(
                getBoolean(ENABLED, true),
                getValue(TIME, "03:00"),
                getInteger(RETENTION, 7),
                backupDirectory.toString(),
                getValue(LAST_BACKUP, "Ainda nao executado")
        );
    }

    @Transactional
    public BackupSettingsDto saveSettings(BackupSettingsRequest request) {
        LocalTime.parse(request.time());
        save(ENABLED, String.valueOf(Boolean.TRUE.equals(request.enabled())));
        save(TIME, request.time());
        save(RETENTION, String.valueOf(request.retention()));
        return getSettings();
    }

    public boolean shouldRun(LocalTime time) {
        BackupSettingsDto settings = getSettings();
        return settings.enabled() && settings.time().equals(time.withSecond(0).withNano(0).toString());
    }

    public boolean hasRun(String runKey) {
        return runKey.equals(getValue(LAST_RUN, ""));
    }

    @Transactional
    public void markRun(String runKey) {
        save(LAST_RUN, runKey);
    }

    public Path getBackupDirectory() {
        return backupDirectory;
    }

    @Transactional
    public void markBackup(String fileName) {
        save(LAST_BACKUP, fileName);
    }

    private String getValue(String key, String fallback) {
        return repository.findById(key).map(AppSetting::getValue).filter(value -> !value.isBlank()).orElse(fallback);
    }

    private boolean getBoolean(String key, boolean fallback) {
        return repository.findById(key).map(AppSetting::getValue).map(Boolean::parseBoolean).orElse(fallback);
    }

    private int getInteger(String key, int fallback) {
        try {
            return Integer.parseInt(getValue(key, String.valueOf(fallback)));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private void save(String key, String value) {
        repository.findById(key).ifPresentOrElse(
                setting -> setting.update(value, Instant.now()),
                () -> repository.save(new AppSetting(key, value, Instant.now()))
        );
    }
}
