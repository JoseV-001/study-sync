package com.josev001.study_sync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.event.EventListener;

import java.awt.Desktop;
import java.net.URI;


// Classe principal da aplicação Spring Boot que inicializa o contexto da aplicação
@EnableScheduling
@ConfigurationPropertiesScan
@SpringBootApplication
public class StudySyncApplication {

    @Value("${study-sync.open-browser:false}")
    private boolean openBrowser;

    public static void main(String[] args) {
        SpringApplication.run(StudySyncApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    void openDashboardWhenConfigured() {
        if (!openBrowser || !Desktop.isDesktopSupported()) {
            return;
        }

        try {
            Desktop.getDesktop().browse(URI.create("http://localhost:8080/"));
        } catch (Exception exception) {
            // Opening a browser is a convenience and must not prevent the app from running.
        }
    }

}
