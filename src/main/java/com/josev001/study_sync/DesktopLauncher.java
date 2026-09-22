package com.josev001.study_sync;

import org.springframework.core.env.SimpleCommandLinePropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.awt.Desktop;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

final class DesktopLauncher {
    private DesktopLauncher() {}

    static boolean reuseRunningApplication(String[] args) {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new SimpleCommandLinePropertySource(args));
        if (!environment.getProperty("study-sync.desktop", Boolean.class, false)
                || environment.getProperty("study-sync.run-once", Boolean.class, false)
                || "none".equals(environment.getProperty("spring.main.web-application-type"))) {
            return false;
        }
        int port = environment.getProperty("server.port", Integer.class, 8080);
        URI dashboard = URI.create("http://localhost:" + port + "/");
        if (!isStudySyncRunning(dashboard)) return false;
        if (environment.getProperty("study-sync.open-browser", Boolean.class, false)) {
            openBrowser(dashboard);
        }
        return true;
    }

    static boolean isStudySyncRunning(URI dashboard) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) dashboard.toURL().openConnection();
            connection.setConnectTimeout(1000);
            connection.setReadTimeout(1000);
            connection.setInstanceFollowRedirects(false);
            if (connection.getResponseCode() != 200) return false;
            try (var stream = connection.getInputStream()) {
                String html = new String(stream.readNBytes(2048), StandardCharsets.UTF_8);
                return html.contains("<title>Study Sync</title>")
                        && html.contains("/assets/jose-victor-logo.png");
            }
        } catch (IOException exception) {
            return false;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    static void openBrowser(URI dashboard) {
        try {
            if (System.getProperty("os.name", "").toLowerCase(Locale.ROOT).startsWith("windows")) {
                new ProcessBuilder("rundll32.exe", "url.dll,FileProtocolHandler", dashboard.toString()).start();
            } else if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(dashboard);
            }
        } catch (IOException exception) {
            System.err.println("Abra o Study Sync em " + dashboard);
        }
    }
}
