package com.josev001.study_sync.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    // Cliente HTTP configurado para realizar requisições ao Clockify.
    @Bean
    public RestClient clockifyRestClient(ClockifyProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build();
    }

    // Cliente HTTP configurado para realizar requisições ao Notion.
    @Bean
    public RestClient notionRestClient(NotionProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader(
                        "Authorization",
                        "Bearer " + properties.getApiKey()
                )
                .defaultHeader(
                        "Notion-Version",
                        "2026-03-11"
                )
                .build();
    }
}