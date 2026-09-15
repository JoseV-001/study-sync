package com.josev001.study_sync.client;

import com.josev001.study_sync.config.ClockifyProperties;
import com.josev001.study_sync.dto.TimeEntryDto;
import com.josev001.study_sync.service.IntegrationSettingsService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import org.springframework.beans.factory.annotation.Qualifier;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


// Cliente responsável por centralizar a integração com o Clockify.

@Component// -> Faz o spring gerenciar o ClockifyClient
public class ClockifyClient {

    private static final int PAGE_SIZE = 1_000;
    private static final ParameterizedTypeReference<List<TimeEntryDto>> TIME_ENTRIES_TYPE =
            new ParameterizedTypeReference<>() {};

    private final RestClient restClient;
    private final ClockifyProperties properties;
    private final IntegrationSettingsService settingsService;


    public ClockifyClient(
            @Qualifier("clockifyRestClient") RestClient restClient,
            ClockifyProperties properties,
            IntegrationSettingsService settingsService
    ) {
        this.restClient = restClient;
        this.properties = properties;
        this.settingsService = settingsService;
    }

    public String getUser() {
        return restClient.get()
                .uri("/user")
                .header("X-Api-Key", getApiKey())
                .retrieve()
                .body(String.class);
    }

    public List<TimeEntryDto> getTimeEntries(Instant start, Instant end) {
        List<TimeEntryDto> entries = new ArrayList<>();

        for (int page = 1; ; page++) {
            int currentPage = page;
            List<TimeEntryDto> pageEntries = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/workspaces/{workspaceId}/user/{userId}/time-entries")
                            .queryParam("start", start)
                            .queryParam("end", end)
                            .queryParam("page", currentPage)
                            .queryParam("page-size", PAGE_SIZE)
                            .build(properties.getWorkspaceId(), properties.getUserId()))
                    .header("X-Api-Key", getApiKey())
                    .retrieve()
                    .body(TIME_ENTRIES_TYPE);

            if (pageEntries == null || pageEntries.isEmpty()) {
                break;
            }

            entries.addAll(pageEntries);
            if (pageEntries.size() < PAGE_SIZE) {
                break;
            }
        }

        return entries;
    }

    private String getApiKey() {
        String apiKey = settingsService.getClockifyApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Clockify API key is not configured");
        }
        return apiKey;
    }

}
