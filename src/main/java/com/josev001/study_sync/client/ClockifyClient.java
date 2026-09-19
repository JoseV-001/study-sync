package com.josev001.study_sync.client;

import com.josev001.study_sync.dto.ClockifyNamedEntityDto;
import com.josev001.study_sync.dto.TimeEntryDto;
import com.josev001.study_sync.dto.UserResponse;
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
    private final IntegrationSettingsService settingsService;


    public ClockifyClient(
            @Qualifier("clockifyRestClient") RestClient restClient,
            IntegrationSettingsService settingsService
    ) {
        this.restClient = restClient;
        this.settingsService = settingsService;
    }

    public UserResponse getUser() {
        return getUser(getApiKey());
    }

    public UserResponse getUser(String apiKey) {
        return restClient.get()
                .uri("/user")
                .header("X-Api-Key", apiKey)
                .retrieve()
                .body(UserResponse.class);
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
                            .build(settingsService.getClockifyWorkspaceId(), settingsService.getClockifyUserId()))
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

    public ClockifyNamedEntityDto getProject(String projectId) {
        return restClient.get()
                .uri("/workspaces/{workspaceId}/projects/{projectId}", settingsService.getClockifyWorkspaceId(), projectId)
                .header("X-Api-Key", getApiKey())
                .retrieve()
                .body(ClockifyNamedEntityDto.class);
    }

    public ClockifyNamedEntityDto getTask(String projectId, String taskId) {
        return restClient.get()
                .uri(
                        "/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}",
                        settingsService.getClockifyWorkspaceId(),
                        projectId,
                        taskId
                )
                .header("X-Api-Key", getApiKey())
                .retrieve()
                .body(ClockifyNamedEntityDto.class);
    }

    public ClockifyNamedEntityDto getTag(String tagId) {
        return restClient.get()
                .uri("/workspaces/{workspaceId}/tags/{tagId}", settingsService.getClockifyWorkspaceId(), tagId)
                .header("X-Api-Key", getApiKey())
                .retrieve()
                .body(ClockifyNamedEntityDto.class);
    }

    private String getApiKey() {
        String apiKey = settingsService.getClockifyApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Clockify API key is not configured");
        }
        return apiKey;
    }

}
