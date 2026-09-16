package com.josev001.study_sync.service;

import com.josev001.study_sync.client.ClockifyClient;
import com.josev001.study_sync.dto.ClockifyEntryMetadataDto;
import com.josev001.study_sync.dto.ClockifyNamedEntityDto;
import com.josev001.study_sync.dto.TimeEntryDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Service
public class ClockifyMetadataService {

    private static final Logger logger = LoggerFactory.getLogger(ClockifyMetadataService.class);

    private final ClockifyClient clockifyClient;
    private final Map<String, String> projectNames = new ConcurrentHashMap<>();
    private final Map<String, String> topicNames = new ConcurrentHashMap<>();
    private final Map<String, String> tagNames = new ConcurrentHashMap<>();

    public ClockifyMetadataService(ClockifyClient clockifyClient) {
        this.clockifyClient = clockifyClient;
    }

    public ClockifyEntryMetadataDto resolve(TimeEntryDto entry) {
        String projectName = resolveProject(entry.projectId());
        String topicName = resolveTopic(entry.projectId(), entry.taskId());
        List<String> resolvedTagNames = entry.tagIds() == null
                ? List.of()
                : entry.tagIds().stream()
                        .filter(this::hasText)
                        .map(this::resolveTag)
                        .filter(this::hasText)
                        .distinct()
                        .toList();

        return new ClockifyEntryMetadataDto(projectName, topicName, resolvedTagNames);
    }

    public void clearCache() {
        projectNames.clear();
        topicNames.clear();
        tagNames.clear();
    }

    private String resolveProject(String projectId) {
        if (!hasText(projectId)) {
            return null;
        }
        return resolveSafely(
                "project",
                projectId,
                () -> projectNames.computeIfAbsent(projectId, id -> nameOf(clockifyClient.getProject(id)))
        );
    }

    private String resolveTopic(String projectId, String taskId) {
        if (!hasText(projectId) || !hasText(taskId)) {
            return null;
        }
        String cacheKey = projectId + ":" + taskId;
        return resolveSafely(
                "topic",
                taskId,
                () -> topicNames.computeIfAbsent(
                        cacheKey,
                        ignored -> nameOf(clockifyClient.getTask(projectId, taskId))
                )
        );
    }

    private String resolveTag(String tagId) {
        return resolveSafely(
                "tag",
                tagId,
                () -> tagNames.computeIfAbsent(tagId, id -> nameOf(clockifyClient.getTag(id)))
        );
    }

    private String resolveSafely(String type, String id, Supplier<String> resolver) {
        try {
            return resolver.get();
        } catch (RuntimeException exception) {
            logger.warn("Could not resolve Clockify {} {}: {}", type, id, exception.getMessage());
            return null;
        }
    }

    private String nameOf(ClockifyNamedEntityDto entity) {
        return entity == null || !hasText(entity.name()) ? null : entity.name().trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
