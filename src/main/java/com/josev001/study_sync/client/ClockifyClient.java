package com.josev001.study_sync.client;

import com.josev001.study_sync.config.ClockifyProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

// Cliente responsável por centralizar a integração com o Clockify.

@Component// -> Faz o spring gerenciar o ClockifyClient
public class ClockifyClient {

    private final RestClient restClient;
    private final ClockifyProperties properties;

    public ClockifyClient(RestClient restClient, ClockifyProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    public String getUser() {
        return restClient.get()
                .uri("/user")
                .header("X-Api-Key", properties.getApiKey())
                .retrieve()
                .body(String.class);
    }

}
