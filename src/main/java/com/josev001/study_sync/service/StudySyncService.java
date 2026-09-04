package com.josev001.study_sync.service;

import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class StudySyncService {

    private final ClockifyService clockifyService;
    private final NotionService notionService;

    public StudySyncService(
            ClockifyService clockifyService,
            NotionService notionService
    ) {
        this.clockifyService = clockifyService;
        this.notionService = notionService;
    }

    // Sincroniza o total de horas estudadas da semana atual com o Notion.
    public String syncCurrentWeek() {

        Duration totalStudyTime =
                clockifyService.getTotalStudyTime();

        return notionService.updateCurrentWeekStudyTime(
                totalStudyTime
        );
    }
}