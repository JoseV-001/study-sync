package com.josev001.study_sync.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record BackupSettingsRequest(
        Boolean enabled,
        @NotBlank(message = "Informe o horario do backup.")
        @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Informe um horario valido.")
        String time,
        @Min(value = 1, message = "Mantenha pelo menos um backup.")
        @Max(value = 30, message = "A retencao pode ter no maximo 30 arquivos.")
        Integer retention
) {
}
