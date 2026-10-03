package com.josev001.study_sync.Scheduler;

import com.josev001.study_sync.service.BackupService;
import com.josev001.study_sync.service.BackupSettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class BackupScheduler {

    private static final Logger logger = LoggerFactory.getLogger(BackupScheduler.class);
    private final BackupService backupService;
    private final BackupSettingsService settingsService;

    public BackupScheduler(BackupService backupService, BackupSettingsService settingsService) {
        this.backupService = backupService;
        this.settingsService = settingsService;
    }

    @Scheduled(cron = "0 * * * * *", zone = "America/Sao_Paulo")
    public void createScheduledBackup() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("America/Sao_Paulo")).withSecond(0).withNano(0);
        if (!settingsService.shouldRun(now.toLocalTime())) {
            return;
        }
        String runKey = now.toLocalDate() + "-" + now.toLocalTime();
        if (settingsService.hasRun(runKey)) {
            return;
        }
        try {
            backupService.createAutomaticBackup();
            settingsService.markRun(runKey);
            logger.info("Backup automatico criado com sucesso.");
        } catch (RuntimeException exception) {
            logger.error("Nao foi possivel criar o backup automatico.", exception);
        }
    }
}
