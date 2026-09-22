package com.josev001.study_sync;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class DesktopLauncherTest {
    @Test
    void reusesExistingDashboardButDoesNotSkipScheduledSync() throws Exception {
        HttpServer server = server("<title>Study Sync</title><img src='/assets/jose-victor-logo.png'>");
        try {
            String port = "--server.port=" + server.getAddress().getPort();
            assertThat(DesktopLauncher.reuseRunningApplication(new String[]{
                    "--study-sync.desktop=true", "--study-sync.open-browser=false", port
            })).isTrue();
            assertThat(DesktopLauncher.reuseRunningApplication(new String[]{
                    "--study-sync.desktop=true", "--study-sync.run-once=true", port
            })).isFalse();
        } finally {
            server.stop(0);
        }
    }

    @Test
    void doesNotMistakeAnotherWebServerForStudySync() throws Exception {
        HttpServer server = server("<title>Other application</title>");
        try {
            assertThat(DesktopLauncher.isStudySyncRunning(
                    URI.create("http://localhost:" + server.getAddress().getPort() + "/"))).isFalse();
        } finally {
            server.stop(0);
        }
    }

    private HttpServer server(String body) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", exchange -> {
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            try (var output = exchange.getResponseBody()) { output.write(bytes); }
        });
        server.start();
        return server;
    }
}
