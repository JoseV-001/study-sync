package com.josev001.study_sync.Scheduler;

import com.josev001.study_sync.dto.SyncResultDto;
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
    private final boolean syncOnStartup;

    public StudySyncScheduler(
            StudySyncService studySyncService,
            @Value("${study-sync.sync-on-startup:true}") boolean syncOnStartup
    ) {
        this.studySyncService = studySyncService;
        this.syncOnStartup = syncOnStartup;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void syncOnStartup() {
        if (syncOnStartup) {
            syncPreviousWeek("ao abrir a aplicação");
        }
    }

    @Scheduled(
            cron = "${study-sync.schedule.cron:0 0 20 * * MON}",
            zone = "${study-sync.schedule.zone:America/Sao_Paulo}"
    )

    public void syncWeeklyStudyTime() {
        syncPreviousWeek("no agendamento de segunda-feira");
    }

    private void syncPreviousWeek(String trigger) {
        try {
        SyncResultDto result = studySyncService.syncPreviousWeek();

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
