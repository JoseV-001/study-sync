package com.josev001.study_sync.client;

import com.josev001.study_sync.config.NotionProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;


@Component
public class NotionClient {

    private final RestClient restClient;
    private final NotionProperties notionProperties;

    public NotionClient(RestClient restClient, NotionProperties notionProperties) {
        this.restClient = restClient;
        this.notionProperties = notionProperties;
    }

}
