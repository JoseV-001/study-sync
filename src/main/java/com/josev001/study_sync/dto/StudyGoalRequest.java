package com.josev001.study_sync.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record StudyGoalRequest(
        @Min(value = 0, message = "A meta diaria nao pode ser negativa")
        @Max(value = 1440, message = "A meta diaria deve ser de no maximo 24 horas")
        int dailyMinutes,
        @Min(value = 0, message = "A meta semanal nao pode ser negativa")
        @Max(value = 10080, message = "A meta semanal deve ser de no maximo 168 horas")
        int weeklyMinutes
) {
}
