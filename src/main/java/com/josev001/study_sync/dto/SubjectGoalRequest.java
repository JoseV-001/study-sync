package com.josev001.study_sync.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SubjectGoalRequest(
        @NotBlank(message = "Informe a materia ou topico")
        @Size(max = 256, message = "A materia ou topico deve ter no maximo 256 caracteres")
        String subject,
        @Min(value = 1, message = "A meta semanal deve ser maior que zero")
        @Max(value = 10080, message = "A meta semanal deve ser de no maximo 168 horas")
        int weeklyMinutes
) {
}
