package com.josev001.study_sync.controller;

import com.josev001.study_sync.dto.SyncResultDto;
import com.josev001.study_sync.dto.SyncHistoryDto;
import com.josev001.study_sync.dto.WeeklyStudyDto;
import com.josev001.study_sync.dto.IntegrationSettingsDto;
import com.josev001.study_sync.dto.IntegrationSettingsRequest;
import com.josev001.study_sync.dto.DiagnosticsDto;
import com.josev001.study_sync.dto.AutomaticSyncSettingsDto;
import com.josev001.study_sync.dto.AutomaticSyncSettingsRequest;
import com.josev001.study_sync.dto.BookDto;
import com.josev001.study_sync.dto.BookRequest;
import com.josev001.study_sync.dto.SubjectReclassificationDto;
import com.josev001.study_sync.dto.BackupSettingsDto;
import com.josev001.study_sync.dto.BackupSettingsRequest;
import com.josev001.study_sync.dto.AutomaticBackupResultDto;
import com.josev001.study_sync.dto.StudyAnalyticsDto;
import com.josev001.study_sync.dto.StudyImportDto;
import com.josev001.study_sync.dto.ClockifyConnectionDto;
import com.josev001.study_sync.dto.ClockifySetupRequest;
import com.josev001.study_sync.dto.ApiErrorDto;
import com.josev001.study_sync.dto.StudyBackupDto;
import com.josev001.study_sync.dto.StudyGoalDto;
import com.josev001.study_sync.dto.StudyGoalProgressDto;
import com.josev001.study_sync.dto.StudyGoalRequest;
import com.josev001.study_sync.dto.SubjectGoalProgressDto;
import com.josev001.study_sync.dto.SubjectGoalRequest;
import com.josev001.study_sync.dto.SubjectSuggestionDto;
import com.josev001.study_sync.dto.UserResponse;
import com.josev001.study_sync.client.ClockifyClient;
import com.josev001.study_sync.service.IntegrationSettingsService;
import com.josev001.study_sync.service.StudyAnalyticsService;
import com.josev001.study_sync.service.StudySyncService;
import com.josev001.study_sync.service.StudyGoalService;
import com.josev001.study_sync.service.StudyEntryService;
import com.josev001.study_sync.service.SubjectGoalService;
import com.josev001.study_sync.service.BackupService;
import com.josev001.study_sync.service.BookService;
import com.josev001.study_sync.service.BackupSettingsService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.core.env.Environment;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.boot.SpringApplication;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@RestController
@RequestMapping("/sync")
public class StudySyncController {

    private final StudySyncService studySyncService;
    private final IntegrationSettingsService settingsService;
    private final StudyAnalyticsService studyAnalyticsService;
    private final ClockifyClient clockifyClient;
    private final StudyGoalService studyGoalService;
    private final StudyEntryService studyEntryService;
    private final SubjectGoalService subjectGoalService;
    private final BackupService backupService;
    private final ConfigurableApplicationContext applicationContext;
    private final Environment environment;
    private final BookService bookService;
    private final BackupSettingsService backupSettingsService;

    public StudySyncController(
            StudySyncService studySyncService,
            IntegrationSettingsService settingsService,
            StudyAnalyticsService studyAnalyticsService,
            ClockifyClient clockifyClient,
            StudyGoalService studyGoalService,
            StudyEntryService studyEntryService,
            SubjectGoalService subjectGoalService,
            BackupService backupService,
            ConfigurableApplicationContext applicationContext,
            Environment environment,
            BookService bookService,
            BackupSettingsService backupSettingsService
    ) {
        this.studySyncService = studySyncService;
        this.settingsService = settingsService;
        this.studyAnalyticsService = studyAnalyticsService;
        this.clockifyClient = clockifyClient;
        this.studyGoalService = studyGoalService;
        this.studyEntryService = studyEntryService;
        this.subjectGoalService = subjectGoalService;
        this.backupService = backupService;
        this.applicationContext = applicationContext;
        this.environment = environment;
        this.bookService = bookService;
        this.backupSettingsService = backupSettingsService;
    }

    @PostMapping("/current-week")
    public ResponseEntity<?> syncCurrentWeek() {
        return syncWeekResponse(studySyncService::syncCurrentWeek);
    }

    @PostMapping("/shutdown")
    public ResponseEntity<Void> shutdown(HttpServletRequest request) {
        if (!isLocalRequest(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Thread shutdownThread = new Thread(
                () -> {
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                    }
                    SpringApplication.exit(applicationContext, () -> 0);
                },
                "study-sync-shutdown"
        );
        shutdownThread.setDaemon(true);
        shutdownThread.start();
        return ResponseEntity.accepted().build();
    }

    private boolean isLocalRequest(HttpServletRequest request) {
        String address = request.getRemoteAddr();
        return "127.0.0.1".equals(address)
                || "0:0:0:0:0:0:0:1".equals(address)
                || "::1".equals(address);
    }

    @PostMapping("/previous-week")
    public ResponseEntity<?> syncPreviousWeek() {
        return syncWeekResponse(studySyncService::syncPreviousWeek);
    }

    @PostMapping("/week")
    public ResponseEntity<?> syncWeek(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate
    ) {
        return syncWeekResponse(() -> studySyncService.syncWeek(startDate));
    }

    @GetMapping("/history")
    public List<SyncHistoryDto> getSyncHistory() {
        return studySyncService.getSyncHistory();
    }

    @GetMapping("/weeks")
    public List<WeeklyStudyDto> getWeeklyStudyHistory(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to
    ) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = from != null
                ? from
                : today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(12);
        LocalDate endDate = to != null ? to : today;
        return studySyncService.getWeeklyStudyHistory(startDate, endDate);
    }

    @GetMapping("/settings")
    public IntegrationSettingsDto getSettings() {
        return settingsService.getSettings();
    }

    @GetMapping("/books")
    public List<BookDto> getBooks() {
        return bookService.findAll();
    }

    @PostMapping("/books")
    public ResponseEntity<?> createBook(@Valid @RequestBody BookRequest request) {
        try {
            return ResponseEntity.ok(bookService.save(request));
        } catch (RuntimeException exception) {
            return ResponseEntity.badRequest().body(new ApiErrorDto(exception.getMessage()));
        }
    }

    @PutMapping("/books/{id}")
    public ResponseEntity<?> updateBook(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
        try {
            return ResponseEntity.ok(bookService.update(id, request));
        } catch (RuntimeException exception) {
            return ResponseEntity.badRequest().body(new ApiErrorDto(exception.getMessage()));
        }
    }

    @DeleteMapping("/books/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable Long id) {
        try {
            bookService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException exception) {
            return ResponseEntity.badRequest().body(new ApiErrorDto(exception.getMessage()));
        }
    }

    @GetMapping("/settings/automatic-sync")
    public AutomaticSyncSettingsDto getAutomaticSyncSettings() {
        return settingsService.getAutomaticSyncSettings();
    }

    @PutMapping("/settings/automatic-sync")
    public ResponseEntity<?> saveAutomaticSyncSettings(
            @Valid @RequestBody AutomaticSyncSettingsRequest request
    ) {
        try {
            return ResponseEntity.ok(settingsService.saveAutomaticSyncSettings(request));
        } catch (RuntimeException exception) {
            return ResponseEntity.badRequest().body(new ApiErrorDto(
                    hasText(exception.getMessage()) ? exception.getMessage() : "Nao foi possivel salvar o agendamento."
            ));
        }
    }

    @GetMapping("/diagnostics")
    public DiagnosticsDto getDiagnostics() {
        String version = StudySyncController.class.getPackage().getImplementationVersion();
        if (!hasText(version)) {
            version = "desenvolvimento";
        }
        int port = environment.getProperty(
                "local.server.port",
                Integer.class,
                environment.getProperty("server.port", Integer.class, 3001)
        );
        String dataDirectory = environment.getProperty("study-sync.data-dir", "nao identificado");
        String mode = Boolean.getBoolean("study-sync.desktop") ? "Executavel Windows" : "Codigo-fonte";
        return new DiagnosticsDto(
                version,
                port,
                dataDirectory,
                Runtime.version().toString(),
                System.getProperty("java.home", "nao identificado"),
                System.getProperty("user.dir", "nao identificado"),
                mode,
                settingsService.getSettings().clockifyConfigured(),
                settingsService.isNotionConfigured()
        );
    }

    @PutMapping("/settings")
    public ResponseEntity<?> saveSettings(
            @RequestBody IntegrationSettingsRequest request
    ) {
        try {
            if (hasText(request.clockifyApiKey())) {
                saveClockifyConnection(request.clockifyApiKey());
            }
            return ResponseEntity.ok(settingsService.saveSettings(request));
        } catch (RuntimeException exception) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(new ApiErrorDto("Nao foi possivel salvar as integracoes. Confira os dados e tente novamente."));
        }
    }

    private ResponseEntity<?> syncWeekResponse(java.util.function.Supplier<SyncResultDto> sync) {
        try {
            return ResponseEntity.ok(sync.get());
        } catch (RuntimeException exception) {
            String message = hasText(exception.getMessage())
                    ? exception.getMessage()
                    : "Nao foi possivel concluir a sincronizacao.";
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(new ApiErrorDto(message));
        }
    }

    @PostMapping("/settings/connect-clockify")
    public ResponseEntity<?> connectClockify(@Valid @RequestBody ClockifySetupRequest request) {
        try {
            return ResponseEntity.ok(saveClockifyConnection(request.apiKey()));
        } catch (RuntimeException exception) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(new ApiErrorDto("Nao foi possivel conectar ao Clockify. Confira a chave e tente novamente."));
        }
    }

    @PostMapping("/settings/test-clockify")
    public ResponseEntity<ClockifyConnectionDto> testClockifyConnection() {
        try {
            clockifyClient.getUser();
            return ResponseEntity.ok(new ClockifyConnectionDto(true, "Conexao com o Clockify validada."));
        } catch (RuntimeException exception) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(new ClockifyConnectionDto(false, "Nao foi possivel validar a chave do Clockify."));
        }
    }

    private IntegrationSettingsDto saveClockifyConnection(String apiKey) {
        UserResponse user = clockifyClient.getUser(apiKey);
        return settingsService.saveClockifyConnection(apiKey, user);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    @GetMapping("/goals")
    public StudyGoalDto getGoals() {
        return studyGoalService.getGoals();
    }

    @PutMapping("/goals")
    public StudyGoalDto saveGoals(@Valid @RequestBody StudyGoalRequest request) {
        return studyGoalService.saveGoals(request);
    }

    @GetMapping("/goals/progress")
    public StudyGoalProgressDto getGoalProgress() {
        return studyGoalService.getProgress();
    }

    @GetMapping("/goals/subjects")
    public List<SubjectGoalProgressDto> getSubjectGoalProgress() {
        return subjectGoalService.getProgress();
    }

    @GetMapping("/subjects")
    public List<SubjectSuggestionDto> getKnownSubjects() {
        return studyEntryService.getKnownSubjects();
    }

    @PostMapping("/subjects/reclassify")
    public SubjectReclassificationDto reclassifySubjects() {
        return new SubjectReclassificationDto(studyEntryService.reclassifyStoredEntries());
    }

    @PostMapping("/goals/subjects")
    public List<SubjectGoalProgressDto> saveSubjectGoal(@Valid @RequestBody SubjectGoalRequest request) {
        return subjectGoalService.save(request);
    }

    @DeleteMapping("/goals/subjects/{id}")
    public ResponseEntity<Void> deleteSubjectGoal(@PathVariable Long id) {
        subjectGoalService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/import")
    public ResponseEntity<?> importStudyEntries(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to
    ) {
        try {
            return ResponseEntity.ok(studySyncService.importStudyEntries(from, to));
        } catch (IllegalStateException exception) {
            return ResponseEntity.badRequest().body(new ApiErrorDto(exception.getMessage()));
        }
    }

    @PostMapping("/import/all")
    public ResponseEntity<?> importAllStudyEntries() {
        try {
            return ResponseEntity.ok(studySyncService.importAllStudyEntries());
        } catch (IllegalStateException exception) {
            return ResponseEntity.badRequest().body(new ApiErrorDto(exception.getMessage()));
        }
    }

    @GetMapping("/analytics")
    public StudyAnalyticsDto getAnalytics(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to
    ) {
        LocalDate today = LocalDate.now();
        return studyAnalyticsService.getAnalytics(
                from != null ? from : today.minusDays(29),
                to != null ? to : today
        );
    }

    @GetMapping(value = "/backup", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StudyBackupDto> downloadBackup() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=study-sync-backup-" + LocalDate.now() + ".json")
                .body(backupService.createBackup());
    }

    @GetMapping("/settings/backups")
    public BackupSettingsDto getBackupSettings() {
        return backupSettingsService.getSettings();
    }

    @PutMapping("/settings/backups")
    public ResponseEntity<?> saveBackupSettings(@Valid @RequestBody BackupSettingsRequest request) {
        try {
            return ResponseEntity.ok(backupSettingsService.saveSettings(request));
        } catch (RuntimeException exception) {
            return ResponseEntity.badRequest().body(new ApiErrorDto(exception.getMessage()));
        }
    }

    @PostMapping("/backup/automatic")
    public ResponseEntity<?> createAutomaticBackup() {
        try {
            return ResponseEntity.ok(backupService.createAutomaticBackup());
        } catch (RuntimeException exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorDto(exception.getMessage()));
        }
    }

    @PostMapping("/backup/restore")
    public ResponseEntity<?> restoreBackup(@RequestBody StudyBackupDto backup) {
        try {
            backupService.restoreBackup(backup);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(new ApiErrorDto(exception.getMessage()));
        }
    }

    @GetMapping(value = "/export/study-entries.csv", produces = "text/csv")
    public ResponseEntity<String> exportStudyEntries() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=study-sync-estudos-" + LocalDate.now() + ".csv")
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(backupService.exportEntriesAsCsv());
    }
}
