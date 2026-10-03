package com.josev001.study_sync.service;

import com.josev001.study_sync.dto.StudyBackupDto;
import com.josev001.study_sync.persistence.StudyEntry;
import com.josev001.study_sync.persistence.StudyEntryRepository;
import com.josev001.study_sync.persistence.StudyGoal;
import com.josev001.study_sync.persistence.StudyGoalRepository;
import com.josev001.study_sync.persistence.SubjectGoal;
import com.josev001.study_sync.persistence.SubjectGoalRepository;
import com.josev001.study_sync.persistence.Book;
import com.josev001.study_sync.persistence.BookRepository;
import com.josev001.study_sync.persistence.SyncRun;
import com.josev001.study_sync.persistence.SyncRunRepository;
import com.josev001.study_sync.persistence.SyncRunStatus;
import com.josev001.study_sync.persistence.WeeklyStudy;
import com.josev001.study_sync.persistence.WeeklyStudyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class BackupService {

    private final StudyEntryRepository studyEntryRepository;
    private final WeeklyStudyRepository weeklyStudyRepository;
    private final SyncRunRepository syncRunRepository;
    private final StudyGoalRepository studyGoalRepository;
    private final SubjectGoalRepository subjectGoalRepository;
    private final BookRepository bookRepository;
    private final ObjectMapper objectMapper;
    private final BackupSettingsService backupSettingsService;

    public BackupService(
            StudyEntryRepository studyEntryRepository,
            WeeklyStudyRepository weeklyStudyRepository,
            SyncRunRepository syncRunRepository,
            StudyGoalRepository studyGoalRepository,
            SubjectGoalRepository subjectGoalRepository,
            BookRepository bookRepository,
            ObjectMapper objectMapper,
            BackupSettingsService backupSettingsService
    ) {
        this.studyEntryRepository = studyEntryRepository;
        this.weeklyStudyRepository = weeklyStudyRepository;
        this.syncRunRepository = syncRunRepository;
        this.studyGoalRepository = studyGoalRepository;
        this.subjectGoalRepository = subjectGoalRepository;
        this.bookRepository = bookRepository;
        this.objectMapper = objectMapper;
        this.backupSettingsService = backupSettingsService;
    }

    @Transactional(readOnly = true)
    public StudyBackupDto createBackup() {
        return new StudyBackupDto(
                StudyBackupDto.FORMAT,
                Instant.now(),
                studyEntryRepository.findAllByOrderByStartedAtAsc().stream().map(this::toEntryBackup).toList(),
                weeklyStudyRepository.findAll().stream().map(this::toWeeklyBackup).toList(),
                syncRunRepository.findAll().stream().map(this::toSyncRunBackup).toList(),
                studyGoalRepository.findById((short) 1)
                        .map(goal -> new StudyBackupDto.StudyGoalBackupDto(goal.getDailyMinutes(), goal.getWeeklyMinutes()))
                        .orElse(null),
                subjectGoalRepository.findAllByOrderBySubjectAsc().stream()
                        .map(goal -> new StudyBackupDto.SubjectGoalBackupDto(goal.getSubject(), goal.getWeeklyMinutes()))
                        .toList(),
                bookRepository.findAllByOrderByStatusAscTargetDateAscTitleAsc().stream()
                        .map(this::toBookBackup)
                        .toList()
        );
    }

    @Transactional(readOnly = true)
    public String exportEntriesAsCsv() {
        StringBuilder csv = new StringBuilder("\uFEFF");
        csv.append("data,hora_inicio,hora_fim,duracao_minutos,materia,topico,projeto,tags,descricao\n");
        for (StudyEntry entry : studyEntryRepository.findAllByOrderByStartedAtAsc()) {
            csv.append(csvValue(entry.getRecordedDate())).append(',')
                    .append(csvValue(entry.getStartedAt())).append(',')
                    .append(csvValue(entry.getEndedAt())).append(',')
                    .append(entry.getDurationMinutes()).append(',')
                    .append(csvValue(entry.getSubject())).append(',')
                    .append(csvValue(entry.getTopicName())).append(',')
                    .append(csvValue(entry.getProjectName())).append(',')
                    .append(csvValue(entry.getTagNames())).append(',')
                    .append(csvValue(entry.getDescription())).append('\n');
        }
        return csv.toString();
    }

    @Transactional
    public com.josev001.study_sync.dto.AutomaticBackupResultDto createAutomaticBackup() {
        Path directory = backupSettingsService.getBackupDirectory();
        try {
            Files.createDirectories(directory);
            String timestamp = LocalDateTime.now(ZoneId.of("America/Sao_Paulo"))
                    .toString().replace(":", "-");
            String fileName = "study-sync-backup-" + timestamp + ".json";
            Path file = directory.resolve(fileName);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), createBackup());

            int retention = backupSettingsService.getSettings().retention();
            List<Path> files;
            try (var stream = Files.list(directory)) {
                files = stream
                        .filter(path -> path.getFileName().toString().startsWith("study-sync-backup-"))
                        .filter(path -> path.getFileName().toString().endsWith(".json"))
                        .sorted(Comparator.comparing(Path::toString).reversed())
                        .toList();
            }
            files.stream().skip(retention).forEach(this::deleteBackupQuietly);
            backupSettingsService.markBackup(fileName);
            return new com.josev001.study_sync.dto.AutomaticBackupResultDto(fileName, directory.toString(), Math.min(files.size(), retention));
        } catch (IOException exception) {
            throw new IllegalStateException("Nao foi possivel criar o backup automatico.", exception);
        }
    }

    private void deleteBackupQuietly(Path file) {
        try {
            Files.deleteIfExists(file);
        } catch (IOException exception) {
            throw new IllegalStateException("Nao foi possivel limpar backups antigos.", exception);
        }
    }

    @Transactional
    public void restoreBackup(StudyBackupDto backup) {
        validateBackup(backup);

        syncRunRepository.deleteAllInBatch();
        weeklyStudyRepository.deleteAllInBatch();
        subjectGoalRepository.deleteAllInBatch();
        bookRepository.deleteAllInBatch();
        studyGoalRepository.deleteAllInBatch();
        studyEntryRepository.deleteAllInBatch();

        studyEntryRepository.saveAll(backup.studyEntries().stream().map(this::toStudyEntry).toList());
        weeklyStudyRepository.saveAll(backup.weeklyStudies().stream().map(this::toWeeklyStudy).toList());
        syncRunRepository.saveAll(backup.syncRuns().stream().map(this::toSyncRun).toList());

        if (backup.studyGoal() != null) {
            studyGoalRepository.save(new StudyGoal(
                    backup.studyGoal().dailyMinutes(),
                    backup.studyGoal().weeklyMinutes(),
                    Instant.now()
            ));
        }
        subjectGoalRepository.saveAll(backup.subjectGoals().stream()
                .map(goal -> new SubjectGoal(goal.subject(), goal.weeklyMinutes(), Instant.now()))
                .toList());
        if (backup.books() != null) {
            bookRepository.saveAll(backup.books().stream()
                    .map(this::toBook)
                    .toList());
        }
    }

    private void validateBackup(StudyBackupDto backup) {
        if (backup == null || (!StudyBackupDto.FORMAT.equals(backup.format())
                && !StudyBackupDto.LEGACY_FORMAT.equals(backup.format()))) {
            throw new IllegalArgumentException("Arquivo de backup invalido ou nao compativel.");
        }
        if (backup.studyEntries() == null || backup.weeklyStudies() == null
                || backup.syncRuns() == null || backup.subjectGoals() == null) {
            throw new IllegalArgumentException("O arquivo de backup esta incompleto.");
        }
    }

    private StudyBackupDto.StudyEntryBackupDto toEntryBackup(StudyEntry entry) {
        return new StudyBackupDto.StudyEntryBackupDto(
                entry.getClockifyEntryId(), entry.getProjectId(), entry.getTaskId(), entry.getProjectName(),
                entry.getTopicName(), entry.getTagIds(), entry.getTagNames(), entry.getDescription(),
                entry.getSubject(), entry.getSubjectSource(), entry.getStartedAt(), entry.getEndedAt(), entry.getDurationMinutes(),
                entry.getRecordedDate(), entry.getSyncedAt()
        );
    }

    private StudyBackupDto.WeeklyStudyBackupDto toWeeklyBackup(WeeklyStudy study) {
        return new StudyBackupDto.WeeklyStudyBackupDto(
                study.getWeekStart(), study.getWeekEnd(), study.getTotalMinutes(), study.getNotionTime(), study.getSyncedAt()
        );
    }

    private StudyBackupDto.SyncRunBackupDto toSyncRunBackup(SyncRun run) {
        return new StudyBackupDto.SyncRunBackupDto(
                run.getWeekStart(), run.getTriggeredBy(), run.getStatus().name(), run.getTotalMinutes(),
                run.getErrorMessage(), run.getCreatedAt(), run.getFinishedAt()
        );
    }

    private StudyEntry toStudyEntry(StudyBackupDto.StudyEntryBackupDto entry) {
        return new StudyEntry(
                entry.clockifyEntryId(), entry.projectId(), entry.taskId(), entry.projectName(), entry.topicName(),
                entry.tagIds(), entry.tagNames(), entry.description(), entry.subject(),
                entry.subjectSource() == null ? "Descricao" : entry.subjectSource(), entry.startedAt(), entry.endedAt(),
                entry.durationMinutes(), entry.recordedDate(), entry.syncedAt()
        );
    }

    private WeeklyStudy toWeeklyStudy(StudyBackupDto.WeeklyStudyBackupDto study) {
        return new WeeklyStudy(study.weekStart(), study.weekEnd(), study.totalMinutes(), study.notionTime(), study.syncedAt());
    }

    private SyncRun toSyncRun(StudyBackupDto.SyncRunBackupDto run) {
        return new SyncRun(
                run.weekStart(), run.triggeredBy(), SyncRunStatus.valueOf(run.status().toUpperCase(Locale.ROOT)),
                run.totalMinutes(), run.errorMessage(), run.createdAt(), run.finishedAt()
        );
    }

    private StudyBackupDto.BookBackupDto toBookBackup(Book book) {
        return new StudyBackupDto.BookBackupDto(
                book.getTitle(), book.getAuthor(), book.getTotalPages(), book.getCurrentPage(), book.getWeeklyPageGoal(),
                book.getStatus(), book.getTargetDate(), book.getCreatedAt(), book.getUpdatedAt()
        );
    }

    private Book toBook(StudyBackupDto.BookBackupDto book) {
        return new Book(
                book.title(), book.author(), book.totalPages(), book.currentPage(), book.weeklyPageGoal(), book.status(),
                book.targetDate(), book.createdAt(), book.updatedAt()
        );
    }

    private String csvValue(Object value) {
        String text = value == null ? "" : value.toString();
        return '"' + text.replace("\"", "\"\"") + '"';
    }
}
