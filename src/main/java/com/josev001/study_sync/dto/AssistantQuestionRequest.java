package com.josev001.study_sync.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AssistantQuestionRequest(
        @NotBlank @Size(max = 500) String question
) {
}
