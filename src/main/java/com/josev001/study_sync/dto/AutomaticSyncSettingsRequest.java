package com.josev001.study_sync.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AutomaticSyncSettingsRequest(
        Boolean enabled,
        Boolean syncOnStartup,
        @NotBlank(message = "Informe o dia da sincronizacao automatica.")
        String dayOfWeek,
        @NotBlank(message = "Informe o horario da sincronizacao automatica.")
        @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Informe um horario valido.")
        String time
) {
}
