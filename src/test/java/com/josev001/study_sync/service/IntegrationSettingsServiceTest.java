package com.josev001.study_sync.service;

import com.josev001.study_sync.config.ClockifyProperties;
import com.josev001.study_sync.config.NotionProperties;
import com.josev001.study_sync.dto.IntegrationSettingsDto;
import com.josev001.study_sync.dto.UserResponse;
import com.josev001.study_sync.persistence.AppSetting;
import com.josev001.study_sync.persistence.AppSettingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class IntegrationSettingsServiceTest {

    @Mock
    private AppSettingRepository appSettingRepository;

    private final Map<String, AppSetting> settings = new HashMap<>();
    private IntegrationSettingsService service;

    @BeforeEach
    void setUp() {
        when(appSettingRepository.findById(anyString()))
                .thenAnswer(invocation -> Optional.ofNullable(settings.get(invocation.getArgument(0))));
        lenient().when(appSettingRepository.save(any(AppSetting.class)))
                .thenAnswer(invocation -> {
                    AppSetting setting = invocation.getArgument(0);
                    settings.put(setting.getKey(), setting);
                    return setting;
                });
        service = new IntegrationSettingsService(
                appSettingRepository,
                new ClockifyProperties(),
                new NotionProperties(),
                false
        );
    }

    @Test
    void savesTheClockifyProfileDetectedFromTheUsersApiKey() {
        UserResponse user = new UserResponse();
        user.setId("clockify-user");
        user.setActiveWorkspace("clockify-workspace");

        IntegrationSettingsDto result = service.saveClockifyConnection("clockify-key", user);

        assertThat(result.clockifyConfigured()).isTrue();
        assertThat(service.getClockifyApiKey()).isEqualTo("clockify-key");
        assertThat(service.getClockifyUserId()).isEqualTo("clockify-user");
        assertThat(service.getClockifyWorkspaceId()).isEqualTo("clockify-workspace");
    }

    @Test
    void usesThePersonalNotionSchemaAsTheDefault() {
        assertThat(service.getNotionDateProperty()).isEqualTo("Data início");
        assertThat(service.getNotionHoursProperty())
                .isEqualTo("Horas na semana (Registro apartir de 20/07)");
    }

    @Test
    void hidesPersonalNotionForARegularInstallation() {
        assertThat(service.getSettings().personalNotionEnabled()).isFalse();
    }

    @Test
    void exposesPersonalNotionWhenOwnerModeIsEnabled() {
        IntegrationSettingsService ownerService = new IntegrationSettingsService(
                appSettingRepository,
                new ClockifyProperties(),
                new NotionProperties(),
                true
        );

        assertThat(ownerService.getSettings().personalNotionEnabled()).isTrue();
    }
}
