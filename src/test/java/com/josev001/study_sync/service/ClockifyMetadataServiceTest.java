package com.josev001.study_sync.service;

import com.josev001.study_sync.client.ClockifyClient;
import com.josev001.study_sync.dto.ClockifyNamedEntityDto;
import com.josev001.study_sync.dto.TimeEntryDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClockifyMetadataServiceTest {

    @Mock
    private ClockifyClient clockifyClient;

    @Test
    void resolvesAndCachesClockifyNames() {
        TimeEntryDto entry = new TimeEntryDto(
                "entry-id",
                null,
                "user-id",
                "project-id",
                "task-id",
                List.of("tag-id"),
                null
        );
        when(clockifyClient.getProject("project-id"))
                .thenReturn(new ClockifyNamedEntityDto("project-id", "Estudo"));
        when(clockifyClient.getTask("project-id", "task-id"))
                .thenReturn(new ClockifyNamedEntityDto("task-id", "Banco de dados"));
        when(clockifyClient.getTag("tag-id"))
                .thenReturn(new ClockifyNamedEntityDto("tag-id", "Faculdade"));
        ClockifyMetadataService service = new ClockifyMetadataService(clockifyClient);

        var first = service.resolve(entry);
        var second = service.resolve(entry);

        assertThat(first.projectName()).isEqualTo("Estudo");
        assertThat(first.topicName()).isEqualTo("Banco de dados");
        assertThat(first.tagNames()).containsExactly("Faculdade");
        assertThat(second).isEqualTo(first);
        verify(clockifyClient, times(1)).getProject("project-id");
        verify(clockifyClient, times(1)).getTask("project-id", "task-id");
        verify(clockifyClient, times(1)).getTag("tag-id");
    }
}
