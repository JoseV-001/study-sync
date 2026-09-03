package com.josev001.study_sync.dto;

import java.util.List;

// Representa a resposta de uma consulta ao Data Source do Notion.
public record NotionQueryResponseDto(
        List<NotionPageDto> results
) {
}