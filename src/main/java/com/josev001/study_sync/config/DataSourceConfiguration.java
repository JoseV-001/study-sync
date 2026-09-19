package com.josev001.study_sync.config;

import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
public class DataSourceConfiguration {

    @Bean
    public DataSource dataSource(DataSourceProperties properties) throws IOException {
        createSqliteParentDirectory(properties.getUrl());
        return properties.initializeDataSourceBuilder().build();
    }

    private void createSqliteParentDirectory(String jdbcUrl) throws IOException {
        if (jdbcUrl == null || !jdbcUrl.startsWith("jdbc:sqlite:")) {
            return;
        }

        String databasePath = jdbcUrl.substring("jdbc:sqlite:".length());
        if (databasePath.isBlank() || databasePath.equals(":memory:") || databasePath.startsWith("file:")) {
            return;
        }

        Path parentDirectory = Path.of(databasePath).toAbsolutePath().getParent();
        if (parentDirectory != null) {
            Files.createDirectories(parentDirectory);
        }
    }
}
