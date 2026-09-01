package com.josev001.study_sync;


import com.josev001.study_sync.client.NotionClient;
import com.josev001.study_sync.service.ClockifyService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;

import java.time.Duration;

// Classe principal da aplicação Spring Boot que inicializa o contexto da aplicação.
@ConfigurationPropertiesScan
@SpringBootApplication
public class StudySyncApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudySyncApplication.class, args);
    }

    @Bean
    CommandLineRunner testWeeklyTotal(
            ClockifyService clockifyService,
            NotionClient notionClient
    ) {
        return args -> {

            String total = clockifyService.getFormattedTotalStudyTime();

            System.out.println("Total da semana: " + total);

            String notionResponse = notionClient.getPage(
                    "7a5ee13f3b8945e587a6debbde110504"
            );

            System.out.println("Resposta Notion:");
            System.out.println(notionResponse);
        };
    }

}