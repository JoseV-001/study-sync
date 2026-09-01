package com.josev001.study_sync.client;

import com.josev001.study_sync.config.NotionProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class NotionClient {

    private final RestClient restClient;
    private final NotionProperties properties;

    public NotionClient(
            @Qualifier("notionRestClient") RestClient restClient,
            NotionProperties properties
    ) {
        this.restClient = restClient;
        this.properties = properties;
    }

    // Busca uma página do Notion pelo seu ID.
    public String getPage(String pageId) {
        return restClient.get()
                .uri("/pages/" + pageId)
                .retrieve()
                .body(String.class);
    }

}
