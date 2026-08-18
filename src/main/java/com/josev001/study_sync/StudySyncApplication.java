package com.josev001.study_sync;

import com.josev001.study_sync.dto.TimeEntryDto;
import com.josev001.study_sync.service.ClockifyService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;

import java.util.List;

// Classe principal da aplicação Spring Boot que inicializa o contexto da aplicação.
@ConfigurationPropertiesScan
@SpringBootApplication
public class StudySyncApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudySyncApplication.class, args);
    }

    @Bean
    CommandLineRunner testClockify(ClockifyService clockifyService) {
        return args -> {
            List<TimeEntryDto> entries = clockifyService.getTimeEntries();

            System.out.println("Quantidade de registros: " + entries.size());

            entries.stream()
                    .limit(5)
                    .forEach(entry ->
                            System.out.println(
                                    entry.description() + " - " +
                                            entry.timeInterval().duration()
                            )
                    );
        };
    }
}