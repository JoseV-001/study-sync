package com.josev001.study_sync.Scheduler;

import com.josev001.study_sync.service.StudySyncService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class StudySyncScheduler {

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

        String syncedTime = studySyncService.syncCurrentWeek();

        System.out.println(
                "Horas sincronizadas automaticamente com o Notion: "
                        + syncedTime
        );
    }
}