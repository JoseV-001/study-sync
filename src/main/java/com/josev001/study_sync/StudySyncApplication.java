package com.josev001.study_sync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.event.EventListener;

import java.net.URI;


// Classe principal da aplicação Spring Boot que inicializa o contexto da aplicação
@EnableScheduling
@ConfigurationPropertiesScan
@SpringBootApplication
public class StudySyncApplication {

    @Value("${study-sync.open-browser:false}")
    private boolean openBrowser;

    public static void main(String[] args) {
        if (DesktopLauncher.reuseRunningApplication(args)) return;
        SpringApplication.run(StudySyncApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    void openDashboardWhenConfigured(ApplicationReadyEvent event) {
        if (!openBrowser) {
            return;
        }

        int port = event.getApplicationContext().getEnvironment()
                .getProperty("local.server.port", Integer.class, 8080);
        DesktopLauncher.openBrowser(URI.create("http://localhost:" + port + "/"));
    }

}
