package com.josev001.study_sync.config;

// Classe de configuração usada para expor os objetos de apoio ao restante da aplicação.

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration // -> Indica que vai definir beans no contexto da aplicação
public class RestClientConfig {

    @Bean
    public RestClient restClient(ClockifyProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build();
    }



}
