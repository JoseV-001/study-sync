package com.josev001.study_sync.controller;

import com.josev001.study_sync.dto.SyncResultDto;
import com.josev001.study_sync.dto.SyncHistoryDto;
import com.josev001.study_sync.dto.WeeklyStudyDto;
import com.josev001.study_sync.dto.IntegrationSettingsDto;
import com.josev001.study_sync.dto.IntegrationSettingsRequest;
import com.josev001.study_sync.service.IntegrationSettingsService;
import com.josev001.study_sync.service.StudySyncService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@RestController
@RequestMapping("/sync")
public class StudySyncController {

    private final StudySyncService studySyncService;
    private final IntegrationSettingsService settingsService;

    public StudySyncController(
            StudySyncService studySyncService,
            IntegrationSettingsService settingsService
    ) {
        this.studySyncService = studySyncService;
        this.settingsService = settingsService;
    }

    @PostMapping("/current-week")
    public SyncResultDto syncCurrentWeek() {
        return studySyncService.syncCurrentWeek();
    }

    @PostMapping("/previous-week")
    public SyncResultDto syncPreviousWeek() {
        return studySyncService.syncPreviousWeek();
    }

    @PostMapping("/week")
    public SyncResultDto syncWeek(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate
    ) {
        return studySyncService.syncWeek(startDate);
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

    @PutMapping("/settings")
    public IntegrationSettingsDto saveSettings(
            @Valid @RequestBody IntegrationSettingsRequest request
    ) {
        return settingsService.saveSettings(request);
    }
}
