package com.josev001.study_sync.service;

import com.josev001.study_sync.config.ClockifyProperties;
import com.josev001.study_sync.config.NotionProperties;
import com.josev001.study_sync.dto.IntegrationSettingsDto;
import com.josev001.study_sync.dto.IntegrationSettingsRequest;
import com.josev001.study_sync.dto.AutomaticSyncSettingsDto;
import com.josev001.study_sync.dto.AutomaticSyncSettingsRequest;
import com.josev001.study_sync.dto.UserResponse;
import com.josev001.study_sync.persistence.AppSetting;
import com.josev001.study_sync.persistence.AppSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Service
public class IntegrationSettingsService {

    private static final String CLOCKIFY_API_KEY = "clockify.api-key";
    private static final String CLOCKIFY_USER_ID = "clockify.user-id";
    private static final String CLOCKIFY_WORKSPACE_ID = "clockify.workspace-id";
    private static final String NOTION_API_KEY = "notion.api-key";
    private static final String NOTION_DATA_SOURCE_ID = "notion.data-source-id";
    private static final String NOTION_DATE_PROPERTY = "notion.date-property";
    private static final String NOTION_HOURS_PROPERTY = "notion.hours-property";
    private static final String AUTO_SYNC_ENABLED = "automatic-sync.enabled";
    private static final String AUTO_SYNC_ON_STARTUP = "automatic-sync.sync-on-startup";
    private static final String AUTO_SYNC_DAY = "automatic-sync.day-of-week";
    private static final String AUTO_SYNC_TIME = "automatic-sync.time";
    private static final String AUTO_SYNC_LAST_RUN = "automatic-sync.last-run";

    private final AppSettingRepository appSettingRepository;
    private final ClockifyProperties clockifyProperties;
    private final NotionProperties notionProperties;
    private final boolean personalNotionEnabled;

    public IntegrationSettingsService(
            AppSettingRepository appSettingRepository,
            ClockifyProperties clockifyProperties,
            NotionProperties notionProperties,
            @Value("${study-sync.personal-notion.enabled:false}") boolean personalNotionEnabled
    ) {
        this.appSettingRepository = appSettingRepository;
        this.clockifyProperties = clockifyProperties;
        this.notionProperties = notionProperties;
        this.personalNotionEnabled = personalNotionEnabled;
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

    public String getNotionDataSourceId() {
        return getSettingValue(NOTION_DATA_SOURCE_ID, notionProperties.getDataSourceId());
    }

    public String getNotionDateProperty() {
        return getSettingValue(NOTION_DATE_PROPERTY, "Data início");
    }

    public String getNotionHoursProperty() {
        return getSettingValue(NOTION_HOURS_PROPERTY, "Horas na semana (Registro apartir de 20/07)");
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
                isNotionConfigured(),
                personalNotionEnabled || isNotionConfigured()
        );
    }

    public boolean isNotionConfigured() {
        return hasText(getNotionApiKey()) && hasText(getNotionDataSourceId());
    }

    public AutomaticSyncSettingsDto getAutomaticSyncSettings() {
        return new AutomaticSyncSettingsDto(
                getBooleanSetting(AUTO_SYNC_ENABLED, true),
                getBooleanSetting(AUTO_SYNC_ON_STARTUP, true),
                getSettingValue(AUTO_SYNC_DAY, DayOfWeek.MONDAY.name()),
                getSettingValue(AUTO_SYNC_TIME, "20:00"),
                "America/Sao_Paulo"
        );
    }

    @Transactional
    public AutomaticSyncSettingsDto saveAutomaticSyncSettings(AutomaticSyncSettingsRequest request) {
        DayOfWeek day = parseDay(request.dayOfWeek());
        LocalTime.parse(request.time());
        saveSetting(AUTO_SYNC_ENABLED, String.valueOf(Boolean.TRUE.equals(request.enabled())));
        saveSetting(AUTO_SYNC_ON_STARTUP, String.valueOf(Boolean.TRUE.equals(request.syncOnStartup())));
        saveSetting(AUTO_SYNC_DAY, day.name());
        saveSetting(AUTO_SYNC_TIME, request.time());
        return getAutomaticSyncSettings();
    }

    public boolean shouldSyncOnStartup() {
        return getAutomaticSyncSettings().enabled() && getAutomaticSyncSettings().syncOnStartup();
    }

    public boolean shouldRunAutomaticSync(DayOfWeek day, LocalTime time) {
        AutomaticSyncSettingsDto settings = getAutomaticSyncSettings();
        return settings.enabled()
                && settings.dayOfWeek().equals(day.name())
                && settings.time().equals(time.withSecond(0).withNano(0).toString());
    }

    public boolean hasAutomaticSyncRun(String dateKey) {
        return dateKey.equals(getSettingValue(AUTO_SYNC_LAST_RUN, ""));
    }

    @Transactional
    public void markAutomaticSyncRun(String dateKey) {
        saveSetting(AUTO_SYNC_LAST_RUN, dateKey);
    }

    @Transactional
    public IntegrationSettingsDto saveSettings(IntegrationSettingsRequest request) {
        if (hasText(request.notionApiKey())) {
            saveSetting(NOTION_API_KEY, request.notionApiKey());
        }
        if (hasText(request.notionDataSourceId())) {
            saveSetting(NOTION_DATA_SOURCE_ID, request.notionDataSourceId());
        }
        if (hasText(request.notionDateProperty())) {
            saveSetting(NOTION_DATE_PROPERTY, request.notionDateProperty());
        }
        if (hasText(request.notionHoursProperty())) {
            saveSetting(NOTION_HOURS_PROPERTY, request.notionHoursProperty());
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

    private boolean getBooleanSetting(String key, boolean fallback) {
        return appSettingRepository.findById(key)
                .map(AppSetting::getValue)
                .map(Boolean::parseBoolean)
                .orElse(fallback);
    }

    private DayOfWeek parseDay(String value) {
        try {
            return DayOfWeek.valueOf(value.trim().toUpperCase());
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Dia da sincronizacao automatica invalido.", exception);
        }
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
