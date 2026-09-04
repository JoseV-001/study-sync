package com.josev001.study_sync.Scheduler;

import com.josev001.study_sync.service.StudySyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class StudySyncScheduler {

    private static final Logger logger =
            LoggerFactory.getLogger(StudySyncScheduler.class);

    private final StudySyncService studySyncService;

    public StudySyncScheduler(StudySyncService studySyncService) {
        this.studySyncService = studySyncService;
    }

    // Sincroniza automaticamente as horas estudadas com o Notion(Aos domingos às 23:30, horário de São Paulo)
    @Scheduled(
            cron = "0 30 23 * * SUN",
            zone = "America/Sao_Paulo"
    )

    public void syncWeeklyStudyTime() {

        try{


        String syncedTime = studySyncService.syncCurrentWeek();

        logger.info(
                "Horas sincronizadas automaticamente com o Notion: "
                        + syncedTime
        );
        }catch(Exception e){

            logger.error(
                    "Erro ao sincronizar horas estudadas com o Notion: "
                            + e.getMessage()
            );
        }
    }
}