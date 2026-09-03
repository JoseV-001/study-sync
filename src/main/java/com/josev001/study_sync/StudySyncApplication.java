package com.josev001.study_sync;


import com.josev001.study_sync.client.NotionClient;
import com.josev001.study_sync.service.ClockifyService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import com.josev001.study_sync.service.NotionService;

import java.time.Duration;

// Classe principal da aplicação Spring Boot que inicializa o contexto da aplicação.
@ConfigurationPropertiesScan
@SpringBootApplication
public class StudySyncApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudySyncApplication.class, args);
    }

    @Bean
    CommandLineRunner syncWeeklyStudyTime(
            ClockifyService clockifyService,
            NotionService notionService
    ) {

        return args -> {

            Duration totalStudyTime =
                    clockifyService.getTotalStudyTime();

            String syncedTime =
                    notionService.updateCurrentWeekStudyTime(
                            totalStudyTime
                    );

            System.out.println(
                    "Horas sincronizadas com o Notion: "
                            + syncedTime
            );
        };
    }
}