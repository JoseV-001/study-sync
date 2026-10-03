package com.josev001.study_sync.service;

import com.josev001.study_sync.dto.AssistantAnswerDto;
import com.josev001.study_sync.dto.BookDto;
import com.josev001.study_sync.dto.StudyAnalyticsDto;
import com.josev001.study_sync.dto.StudyGoalProgressDto;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;

@Service
public class LocalAssistantService {

    private static final ZoneId APP_ZONE = ZoneId.of("America/Sao_Paulo");
    private static final List<String> DEFAULT_SUGGESTIONS = List.of(
            "Quanto estudei hoje?",
            "Qual materia estudei mais este mes?",
            "Como estao minhas metas?",
            "Quais livros estou lendo?"
    );

    private final StudyGoalService goalService;
    private final StudyAnalyticsService analyticsService;
    private final BookService bookService;
    private final IntegrationSettingsService integrationSettingsService;
    private final BackupSettingsService backupSettingsService;

    public LocalAssistantService(
            StudyGoalService goalService,
            StudyAnalyticsService analyticsService,
            BookService bookService,
            IntegrationSettingsService integrationSettingsService,
            BackupSettingsService backupSettingsService
    ) {
        this.goalService = goalService;
        this.analyticsService = analyticsService;
        this.bookService = bookService;
        this.integrationSettingsService = integrationSettingsService;
        this.backupSettingsService = backupSettingsService;
    }

    public AssistantAnswerDto answer(String question) {
        String text = normalize(question);
        LocalDate today = LocalDate.now(APP_ZONE);

        if (containsAny(text, "livro", "leitura", "pagina")) {
            return books(containsAny(text, "concluido", "terminei", "finalizei"));
        }
        if (containsAny(text, "backup", "copia de seguranca")) {
            var settings = backupSettingsService.getSettings();
            return reply(settings.enabled()
                            ? "Os backups automaticos estao ativos para " + settings.time()
                                    + ". O ultimo arquivo registrado e: " + settings.lastBackup() + "."
                            : "Os backups automaticos estao desativados. Voce pode alterar isso em Configuracoes > Backup.",
                    "Abrir configuracoes", "Como exportar meus dados?");
        }
        if (containsAny(text, "notion", "clockify", "integracao", "conectado")) {
            var settings = integrationSettingsService.getSettings();
            return reply("Clockify: " + configured(settings.clockifyConfigured())
                            + ". Notion: " + configured(settings.notionConfigured())
                            + (settings.personalNotionEnabled() ? ". A integracao pessoal com Notion esta habilitada." : "."),
                    "Como configurar o Clockify?", "Para que serve o Notion?");
        }
        if (containsAny(text, "sincronizacao automatica", "sincronizacao", "sincronizar", "sync automatico")) {
            var settings = integrationSettingsService.getAutomaticSyncSettings();
            return reply(settings.enabled()
                            ? "A sincronizacao automatica esta ativa: " + dayLabel(settings.dayOfWeek()) + " as " + settings.time()
                                    + " (" + settings.zone() + ")."
                            : "A sincronizacao automatica esta desativada. Voce ainda pode iniciar uma sincronizacao manual na aba Sincronizacoes.",
                    "Abrir sincronizacoes", "Como importar todo meu historico?");
        }
        if (containsAny(text, "meta", "objetivo")) {
            return goals(goalService.getProgress());
        }

        boolean previousMonth = text.contains("mes passado");
        boolean month = containsAny(text, "mes", "mensal");
        boolean yesterday = text.contains("ontem");
        boolean todayOnly = containsAny(text, "hoje", "diario", "dia de hoje");
        boolean previousWeek = text.contains("semana passada");
        boolean week = containsAny(text, "semana", "semanal");
        LocalDate selectedDay = yesterday ? today.minusDays(1) : today;
        YearMonth selectedMonth = YearMonth.from(today).minusMonths(previousMonth ? 1 : 0);
        LocalDate currentWeekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate from = month ? selectedMonth.atDay(1)
                : todayOnly || yesterday ? selectedDay
                : week ? currentWeekStart.minusWeeks(previousWeek ? 1 : 0)
                : today.minusDays(29);
        LocalDate to = month ? (previousMonth ? selectedMonth.atEndOfMonth() : today)
                : todayOnly || yesterday ? selectedDay
                : week ? from.plusDays(6).isBefore(today) ? from.plusDays(6) : today
                : today;
        StudyAnalyticsDto analytics = analyticsService.getAnalytics(from, to);

        if (containsAny(text, "materia", "assunto", "topico", "estudei mais")) {
            var top = analytics.topSubject();
            return reply(top == null || top.label() == null
                            ? "Ainda nao ha registros de estudo nesse periodo para identificar uma materia principal."
                            : "Sua materia mais estudada no periodo foi " + top.label() + ", com " + formatMinutes(top.totalMinutes()) + ".",
                    "Ver analises", "Quais materias estudei este mes?");
        }
        if (containsAny(text, "dia da semana", "dia que mais", "dia mais", "dia menos", "menos estudei")) {
            boolean least = containsAny(text, "menos", "pior");
            var dayInsight = least ? analytics.leastStudiedDay() : analytics.mostStudiedDay();
            return reply(dayInsight == null || dayInsight.label() == null
                            ? "Ainda nao ha registros suficientes para comparar os dias da semana."
                            : "O dia da semana em que voce " + (least ? "menos" : "mais") + " estudou foi " + dayInsight.label() + ", com " + formatMinutes(dayInsight.totalMinutes()) + " no periodo.",
                    "Ver analises", "Qual horario rendo mais?");
        }
        if (containsAny(text, "horario", "hora que", "foco")) {
            var peak = analytics.peakStudyHour();
            return reply(peak == null || peak.label() == null
                            ? "Ainda nao ha registros suficientes para identificar seu horario de maior foco."
                            : "Seu horario de maior foco foi " + peak.label() + ", com " + formatMinutes(peak.totalMinutes()) + " no periodo.",
                    "Ver analises", "Qual dia da semana estudei mais?");
        }
        if (containsAny(text, "hoje", "semana", "mes", "periodo", "horas", "estudei", "estudo", "total")) {
            String label = yesterday ? "ontem" : previousWeek ? "na semana passada" : week ? "nesta semana"
                    : previousMonth ? "no mes passado" : month ? "neste mes" : "nos ultimos 30 dias";
            return reply("Voce estudou " + formatMinutes(analytics.totalMinutes()) + " " + label
                            + " em " + analytics.activeDays() + (analytics.activeDays() == 1 ? " dia ativo." : " dias ativos."),
                    "Ver analises", "Qual materia estudei mais?");
        }

        return new AssistantAnswerDto(
                "Ainda nao sei responder essa pergunta. Posso consultar seus estudos, metas, materias, livros, backups, integracoes e sincronizacao.",
                DEFAULT_SUGGESTIONS
        );
    }

    private AssistantAnswerDto books(boolean completedOnly) {
        List<BookDto> books = bookService.findAll().stream()
                .filter(book -> "COMPLETED".equals(book.status()) == completedOnly)
                .toList();
        if (books.isEmpty()) {
            return completedOnly
                    ? reply("Voce ainda nao marcou livros como concluidos.", "Abrir livros", "Quais livros estou lendo?")
                    : reply("Voce nao tem livros em andamento no momento. Cadastre um na aba Livros.", "Abrir livros", "Como adiciono um livro?");
        }
        String summary = books.stream().limit(5)
                .map(book -> book.title() + " (pagina " + book.currentPage() + "/" + book.totalPages() + ", " + book.progressPercentage() + "%)")
                .collect(java.util.stream.Collectors.joining("; "));
        return reply("Seus livros ainda nao concluidos: " + summary + (books.size() > 5 ? "; e mais " + (books.size() - 5) + "." : "."),
                "Abrir livros", "Como esta meu progresso de leitura?");
    }

    private AssistantAnswerDto goals(StudyGoalProgressDto progress) {
        String daily = progress.dailyGoalMinutes() > 0
                ? formatMinutes(progress.todayMinutes()) + " de " + formatMinutes(progress.dailyGoalMinutes())
                : "meta diaria ainda nao configurada";
        String weekly = progress.weeklyGoalMinutes() > 0
                ? formatMinutes(progress.weekMinutes()) + " de " + formatMinutes(progress.weeklyGoalMinutes())
                : "meta semanal ainda nao configurada";
        return reply("Hoje: " + daily + ". Nesta semana: " + weekly + ".", "Configurar metas", "Quanto estudei hoje?");
    }

    private AssistantAnswerDto reply(String answer, String... suggestions) {
        return new AssistantAnswerDto(answer, List.of(suggestions));
    }

    private String formatMinutes(long minutes) {
        return (minutes / 60) + "h " + String.format(Locale.ROOT, "%02d", minutes % 60) + "min";
    }

    private String configured(boolean configured) {
        return configured ? "configurado" : "nao configurado";
    }

    private boolean containsAny(String text, String... terms) {
        return java.util.Arrays.stream(terms).anyMatch(text::contains);
    }

    private String normalize(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }

    private String dayLabel(String day) {
        return switch (DayOfWeek.valueOf(day)) {
            case MONDAY -> "segunda-feira";
            case TUESDAY -> "terca-feira";
            case WEDNESDAY -> "quarta-feira";
            case THURSDAY -> "quinta-feira";
            case FRIDAY -> "sexta-feira";
            case SATURDAY -> "sabado";
            case SUNDAY -> "domingo";
        };
    }
}
