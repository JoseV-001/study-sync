package com.josev001.study_sync.dto;

import java.util.List;

public record AssistantAnswerDto(String answer, List<String> suggestions) {
}
