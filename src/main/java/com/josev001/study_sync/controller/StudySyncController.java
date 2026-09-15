package com.josev001.study_sync.controller;

import com.josev001.study_sync.dto.SyncResultDto;
import com.josev001.study_sync.service.StudySyncService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/sync")
public class StudySyncController {

    private final StudySyncService studySyncService;

    public StudySyncController(StudySyncService studySyncService) {
        this.studySyncService = studySyncService;
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
}
