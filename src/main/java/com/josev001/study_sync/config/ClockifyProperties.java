package com.josev001.study_sync.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

// Classe de configuração que mapeia as propriedades do Clockify.
@ConfigurationProperties(prefix = "clockify")
public class ClockifyProperties {

    private String apiKey;
    private String userId;
    private String workspaceId;
    private String baseUrl;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
