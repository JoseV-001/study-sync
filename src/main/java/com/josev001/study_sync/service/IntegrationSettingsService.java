package com.josev001.study_sync.service;

import com.josev001.study_sync.config.ClockifyProperties;
import com.josev001.study_sync.config.NotionProperties;
import com.josev001.study_sync.dto.IntegrationSettingsDto;
import com.josev001.study_sync.dto.IntegrationSettingsRequest;
import com.josev001.study_sync.dto.UserResponse;
import com.josev001.study_sync.persistence.AppSetting;
import com.josev001.study_sync.persistence.AppSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class IntegrationSettingsService {

    private static final String CLOCKIFY_API_KEY = "clockify.api-key";
    private static final String CLOCKIFY_USER_ID = "clockify.user-id";
    private static final String CLOCKIFY_WORKSPACE_ID = "clockify.workspace-id";
    private static final String NOTION_API_KEY = "notion.api-key";

    private final AppSettingRepository appSettingRepository;
    private final ClockifyProperties clockifyProperties;
    private final NotionProperties notionProperties;

    public IntegrationSettingsService(
            AppSettingRepository appSettingRepository,
            ClockifyProperties clockifyProperties,
            NotionProperties notionProperties
    ) {
        this.appSettingRepository = appSettingRepository;
        this.clockifyProperties = clockifyProperties;
        this.notionProperties = notionProperties;
    }

    public String getClockifyApiKey() {
        return appSettingRepository.findById(CLOCKIFY_API_KEY)
                .map(AppSetting::getValue)
                .filter(this::hasText)
                .orElse(clockifyProperties.getApiKey());
    }

    public String getNotionApiKey() {
        return appSettingRepository.findById(NOTION_API_KEY)
                .map(AppSetting::getValue)
                .filter(this::hasText)
                .orElse(notionProperties.getApiKey());
    }

    public String getClockifyUserId() {
        return getSettingValue(CLOCKIFY_USER_ID, clockifyProperties.getUserId());
    }

    public String getClockifyWorkspaceId() {
        return getSettingValue(CLOCKIFY_WORKSPACE_ID, clockifyProperties.getWorkspaceId());
    }

    public IntegrationSettingsDto getSettings() {
        return new IntegrationSettingsDto(
                hasText(getClockifyApiKey())
                        && hasText(getClockifyUserId())
                        && hasText(getClockifyWorkspaceId()),
                hasText(getNotionApiKey())
        );
    }

    @Transactional
    public IntegrationSettingsDto saveSettings(IntegrationSettingsRequest request) {
        if (hasText(request.notionApiKey())) {
            saveSetting(NOTION_API_KEY, request.notionApiKey());
        }
        return getSettings();
    }

    @Transactional
    public IntegrationSettingsDto saveClockifyConnection(String apiKey, UserResponse user) {
        if (!hasText(apiKey) || user == null || !hasText(user.getId()) || !hasText(user.getActiveWorkspace())) {
            throw new IllegalArgumentException("Clockify connection did not return a user and workspace");
        }

        saveSetting(CLOCKIFY_API_KEY, apiKey);
        saveSetting(CLOCKIFY_USER_ID, user.getId());
        saveSetting(CLOCKIFY_WORKSPACE_ID, user.getActiveWorkspace());
        return getSettings();
    }

    private String getSettingValue(String key, String fallback) {
        return appSettingRepository.findById(key)
                .map(AppSetting::getValue)
                .filter(this::hasText)
                .orElse(fallback);
    }

    private void saveSetting(String key, String value) {
        appSettingRepository.findById(key)
                .ifPresentOrElse(
                        setting -> setting.update(value.trim(), Instant.now()),
                        () -> appSettingRepository.save(new AppSetting(key, value.trim(), Instant.now()))
                );
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
