package com.josev001.study_sync.client;

import com.josev001.study_sync.service.IntegrationSettingsService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ClockifyClientTest {
    @Test
    void retrievesEveryPageUsingTheExplicitHistoricalStart() {
        var settings = mock(IntegrationSettingsService.class);
        when(settings.getClockifyWorkspaceId()).thenReturn("workspace");
        when(settings.getClockifyUserId()).thenReturn("user");
        when(settings.getClockifyApiKey()).thenReturn("test-key");
        var builder = RestClient.builder().baseUrl("https://api.clockify.me/api/v1");
        var server = MockRestServiceServer.bindTo(builder).build();
        String fullPage = "[" + String.join(",", Collections.nCopies(1000, "{\"id\":\"entry\"}")) + "]";
        server.expect(queryParam("page", "1"))
                .andExpect(queryParam("start", "1970-01-01T00:00:00Z"))
                .andRespond(withSuccess(fullPage, MediaType.APPLICATION_JSON));
        server.expect(queryParam("page", "2"))
                .andExpect(queryParam("start", "1970-01-01T00:00:00Z"))
                .andRespond(withSuccess("[{\"id\":\"oldest\"}]", MediaType.APPLICATION_JSON));

        var result = new ClockifyClient(builder.build(), settings).getTimeEntries(Instant.EPOCH, Instant.now());

        assertThat(result).hasSize(1001);
        assertThat(result.getLast().id()).isEqualTo("oldest");
        server.verify();
    }
}
