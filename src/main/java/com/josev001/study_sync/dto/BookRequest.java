package com.josev001.study_sync.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record BookRequest(
        @NotBlank(message = "Informe o nome do livro.")
        @Size(max = 256, message = "O nome do livro deve ter no maximo 256 caracteres.")
        String title,
        @Size(max = 256, message = "O autor deve ter no maximo 256 caracteres.")
        String author,
        @Min(value = 1, message = "O livro precisa ter pelo menos uma pagina.")
        @Max(value = 1000000, message = "O livro nao pode ter mais de 1 milhao de paginas.")
        int totalPages,
        @Min(value = 0, message = "A pagina atual nao pode ser negativa.")
        int currentPage,
        @Min(value = 0, message = "A meta de paginas nao pode ser negativa.")
        @Max(value = 1000000, message = "A meta de paginas e muito alta.")
        int weeklyPageGoal,
        @NotBlank(message = "Informe o status do livro.")
        String status,
        LocalDate targetDate
) {
}
