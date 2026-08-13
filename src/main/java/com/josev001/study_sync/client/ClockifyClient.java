package com.josev001.study_sync.client;

import com.josev001.study_sync.config.ClockifyProperties;

// Cliente responsável por centralizar a integração com o Clockify.
public class ClockifyClient {

    private final ClockifyProperties properties;

    public ClockifyClient(ClockifyProperties properties) {
        this.properties = properties;
    }

}
