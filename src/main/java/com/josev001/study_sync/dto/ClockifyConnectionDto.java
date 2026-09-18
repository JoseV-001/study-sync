package com.josev001.study_sync.dto;

public record ClockifyConnectionDto(
        boolean connected,
        String message
) {
}
