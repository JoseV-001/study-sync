package com.josev001.study_sync;

import com.josev001.study_sync.client.ClockifyClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;

// Classe principal da aplicação Spring Boot que inicializa o contexto da aplicação.
@ConfigurationPropertiesScan
@SpringBootApplication
public class StudySyncApplication {

	public static void main(String[] args) {
		SpringApplication.run(StudySyncApplication.class, args);
	}

	@Bean
	CommandLineRunner testClockify(ClockifyClient clockifyClient) {
		return args -> {
			System.out.println("Time Entries: " + clockifyClient.getTimeEntries());
		};
	}

}
