package com.josev001.study_sync.Scheduler;

import com.josev001.study_sync.dto.SyncResultDto;
import com.josev001.study_sync.service.IntegrationSettingsService;
import com.josev001.study_sync.service.StudySyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class StudySyncScheduler {

    private static final Logger logger =
            LoggerFactory.getLogger(StudySyncScheduler.class);

    private final StudySyncService studySyncService;
    private final IntegrationSettingsService settingsService;
    private final boolean syncOnStartup;
    private final boolean scheduleEnabled;

    public StudySyncScheduler(
            StudySyncService studySyncService,
            IntegrationSettingsService settingsService,
            @Value("${study-sync.sync-on-startup:true}") boolean syncOnStartup,
            @Value("${study-sync.schedule.enabled:true}") boolean scheduleEnabled
    ) {
        this.studySyncService = studySyncService;
        this.settingsService = settingsService;
        this.syncOnStartup = syncOnStartup;
        this.scheduleEnabled = scheduleEnabled;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void syncOnStartup() {
        if (syncOnStartup) {
            syncPreviousWeek("startup");
        }
    }

    @Scheduled(
            cron = "${study-sync.schedule.cron:0 0 20 * * MON}",
            zone = "${study-sync.schedule.zone:America/Sao_Paulo}"
    )

    public void syncWeeklyStudyTime() {
        if (!scheduleEnabled) {
            return;
        }

        syncPreviousWeek("scheduler");
    }

    private void syncPreviousWeek(String trigger) {
        if (!settingsService.getSettings().clockifyConfigured()) {
            logger.info("Sincronizacao {} ignorada: Clockify ainda nao foi configurado.", trigger);
            return;
        }

        try {
        SyncResultDto result = studySyncService.syncPreviousWeek(trigger);

        logger.info(
                "Horas sincronizadas {} com o Notion para a semana "
                        + result.weekStartDate() + " a " + result.weekEndDate()
                        + ": " + result.syncedTime(),
                trigger
        );
        } catch (Exception e) {
            logger.error(
                    "Erro ao sincronizar horas estudadas com o Notion",
                    e
            );
        }
    }
}
