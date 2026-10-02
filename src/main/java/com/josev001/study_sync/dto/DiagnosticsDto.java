package com.josev001.study_sync.dto;

public record DiagnosticsDto(
        String version,
        int port,
        String dataDirectory,
        String javaVersion,
        String javaHome,
        String workingDirectory,
        String mode,
        boolean clockifyConfigured,
        boolean notionConfigured
) {
}
