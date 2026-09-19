package com.josev001.study_sync.service;

import com.josev001.study_sync.dto.StudyBackupDto;
import com.josev001.study_sync.persistence.StudyEntry;
import com.josev001.study_sync.persistence.StudyEntryRepository;
import com.josev001.study_sync.persistence.StudyGoal;
import com.josev001.study_sync.persistence.StudyGoalRepository;
import com.josev001.study_sync.persistence.SubjectGoal;
import com.josev001.study_sync.persistence.SubjectGoalRepository;
import com.josev001.study_sync.persistence.SyncRun;
import com.josev001.study_sync.persistence.SyncRunRepository;
import com.josev001.study_sync.persistence.SyncRunStatus;
import com.josev001.study_sync.persistence.WeeklyStudy;
import com.josev001.study_sync.persistence.WeeklyStudyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Service
public class BackupService {

    private final StudyEntryRepository studyEntryRepository;
    private final WeeklyStudyRepository weeklyStudyRepository;
    private final SyncRunRepository syncRunRepository;
    private final StudyGoalRepository studyGoalRepository;
    private final SubjectGoalRepository subjectGoalRepository;

    public BackupService(
            StudyEntryRepository studyEntryRepository,
            WeeklyStudyRepository weeklyStudyRepository,
            SyncRunRepository syncRunRepository,
            StudyGoalRepository studyGoalRepository,
            SubjectGoalRepository subjectGoalRepository
    ) {
        this.studyEntryRepository = studyEntryRepository;
        this.weeklyStudyRepository = weeklyStudyRepository;
        this.syncRunRepository = syncRunRepository;
        this.studyGoalRepository = studyGoalRepository;
        this.subjectGoalRepository = subjectGoalRepository;
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
    public void restoreBackup(StudyBackupDto backup) {
        validateBackup(backup);

        syncRunRepository.deleteAllInBatch();
        weeklyStudyRepository.deleteAllInBatch();
        subjectGoalRepository.deleteAllInBatch();
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
    }

    private void validateBackup(StudyBackupDto backup) {
        if (backup == null || !StudyBackupDto.FORMAT.equals(backup.format())) {
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
                entry.getSubject(), entry.getStartedAt(), entry.getEndedAt(), entry.getDurationMinutes(),
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
                entry.tagIds(), entry.tagNames(), entry.description(), entry.subject(), entry.startedAt(), entry.endedAt(),
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

    private String csvValue(Object value) {
        String text = value == null ? "" : value.toString();
        return '"' + text.replace("\"", "\"\"") + '"';
    }
}
