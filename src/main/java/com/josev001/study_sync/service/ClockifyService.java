package com.josev001.study_sync.service;

import com.josev001.study_sync.client.ClockifyClient;
import com.josev001.study_sync.dto.TimeEntryDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClockifyService {

    private final ClockifyClient clockifyClient;

    public ClockifyService(ClockifyClient clockifyClient) {
        this.clockifyClient = clockifyClient;
    }

    public List<TimeEntryDto> getTimeEntries() {
        return clockifyClient.getTimeEntries();
    }

}
