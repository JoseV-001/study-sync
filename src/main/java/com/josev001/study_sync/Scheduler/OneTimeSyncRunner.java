package com.josev001.study_sync.Scheduler;

import com.josev001.study_sync.service.IntegrationSettingsService;
import com.josev001.study_sync.service.StudySyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "study-sync.run-once", havingValue = "true")
public class OneTimeSyncRunner implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(OneTimeSyncRunner.class);

    private final StudySyncService studySyncService;
    private final IntegrationSettingsService settingsService;
    private final ConfigurableApplicationContext applicationContext;

    public OneTimeSyncRunner(
            StudySyncService studySyncService,
            IntegrationSettingsService settingsService,
            ConfigurableApplicationContext applicationContext
    ) {
        this.studySyncService = studySyncService;
        this.settingsService = settingsService;
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(ApplicationArguments args) {
        int exitCode = 0;

        try {
            if (!settingsService.getSettings().clockifyConfigured()) {
                logger.info("Sincronizacao do Windows ignorada: Clockify ainda nao foi configurado.");
            } else {
                studySyncService.syncPreviousWeek("windows-task");
                logger.info("Sincronizacao do Windows concluida.");
            }
        } catch (RuntimeException exception) {
            exitCode = 1;
            logger.error("Sincronizacao do Windows falhou.", exception);
        }

        int finalExitCode = exitCode;
        System.exit(SpringApplication.exit(applicationContext, () -> finalExitCode));
    }
}
