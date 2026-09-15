package com.josev001.study_sync.client;

import com.josev001.study_sync.config.NotionProperties;
import com.josev001.study_sync.dto.NotionQueryResponseDto;
import com.josev001.study_sync.service.IntegrationSettingsService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
public class NotionClient {

    private final RestClient restClient;
    private final NotionProperties properties;
    private final IntegrationSettingsService settingsService;

    public NotionClient(
            @Qualifier("notionRestClient") RestClient restClient,
            NotionProperties properties,
            IntegrationSettingsService settingsService
    ) {
        this.restClient = restClient;
        this.properties = properties;
        this.settingsService = settingsService;
    }

    // Busca os registros existentes no Planejamento Semanal.
    public String queryWeeklyPlanning() {
        return restClient.post()
                .uri("/data_sources/" + properties.getDataSourceId() + "/query")
                .header("Authorization", "Bearer " + getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of())
                .retrieve()
                .body(String.class);
    }

    // Busca no Planejamento Semanal o registro correspondente à data informada.
    public NotionQueryResponseDto queryWeeklyPlanning(LocalDate startOfWeek) {

        Map<String, Object> body = Map.of(
                "filter", Map.of(
                        "property", "Data início",
                        "date", Map.of(
                                "equals", startOfWeek.toString()
                        )
                )
        );

        return restClient.post()
                .uri("/data_sources/" + properties.getDataSourceId() + "/query")
                .header("Authorization", "Bearer " + getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(NotionQueryResponseDto.class);
    }

    // Atualiza o campo de horas estudadas de uma página semanal no Notion.
    public void updateStudyHours(String pageId, String studyTime) {

        Map<String, Object> body = Map.of(
                "properties", Map.of(
                        "Horas na semana (Registro apartir de 20/07)",
                        Map.of(
                                "rich_text", List.of(
                                        Map.of(
                                                "type", "text",
                                                "text", Map.of(
                                                        "content", studyTime
                                                )
                                        )
                                )
                        )
                )
        );

        restClient.patch()
                .uri("/pages/" + pageId)
                .header("Authorization", "Bearer " + getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    private String getApiKey() {
        String apiKey = settingsService.getNotionApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Notion API key is not configured");
        }
        return apiKey;
    }

}
