package com.josev001.study_sync;

import org.springframework.core.env.SimpleCommandLinePropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.awt.Desktop;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.springframework.context.ConfigurableApplicationContext;

final class DesktopLauncher {
    private static FileChannel instanceLockChannel;
    private static FileLock instanceLock;
    private static int requestedPort = 3001;
    private static int selectedPort = 3001;

    private DesktopLauncher() {}

    static String[] prepareDesktopArgs(String[] args) {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new SimpleCommandLinePropertySource(args));
        if (!environment.getProperty("study-sync.desktop", Boolean.class, false)) return args;

        requestedPort = environment.getProperty("server.port", Integer.class, 3001);
        selectedPort = requestedPort;
        if (requestedPort == 0 || isPortAvailable(requestedPort)
                || isStudySyncRunning(URI.create("http://localhost:" + requestedPort + "/"))) return args;

        for (int port = Math.max(1, requestedPort + 1); port <= Math.min(65535, requestedPort + 30); port++) {
            if (isPortAvailable(port)) {
                selectedPort = port;
                java.util.List<String> updated = new java.util.ArrayList<>();
                for (int i = 0; i < args.length; i++) {
                    if (args[i].startsWith("--server.port=")) continue;
                    if (args[i].equals("--server.port")) { i++; continue; }
                    updated.add(args[i]);
                }
                updated.add("--server.port=" + port);
                return updated.toArray(String[]::new);
            }
        }

        javax.swing.JOptionPane.showMessageDialog(null,
                "As portas a partir de " + requestedPort + " estao ocupadas. Feche outro aplicativo e tente novamente.",
                "Nao foi possivel iniciar o Study Sync", javax.swing.JOptionPane.ERROR_MESSAGE);
        return null;
    }

    static boolean didSelectFallbackPort() { return selectedPort != requestedPort; }
    static int getRequestedPort() { return requestedPort; }
    static int getSelectedPort() { return selectedPort; }

    private static boolean isPortAvailable(int port) {
        try (ServerSocket socket = new ServerSocket()) {
            socket.setReuseAddress(false);
            socket.bind(new InetSocketAddress(port));
            return true;
        } catch (IOException exception) {
            return false;
        }
    }

    static boolean reuseRunningApplication(String[] args) {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new SimpleCommandLinePropertySource(args));
        if (!environment.getProperty("study-sync.desktop", Boolean.class, false)
                || environment.getProperty("study-sync.run-once", Boolean.class, false)
                || "none".equals(environment.getProperty("spring.main.web-application-type"))) {
            return false;
        }
        int port = environment.getProperty("server.port", Integer.class, 3001);
        URI dashboard = URI.create("http://localhost:" + port + "/");
        if (isStudySyncRunning(dashboard)) {
            if (environment.getProperty("study-sync.open-browser", Boolean.class, false)) {
                openBrowser(dashboard);
            }
            return true;
        }

        if (!acquireInstanceLock(environment)) {
            waitForApplication(dashboard);
            if (environment.getProperty("study-sync.open-browser", Boolean.class, false)) {
                openBrowser(dashboard);
            }
            return true;
        }

        return false;
    }

    private static boolean acquireInstanceLock(StandardEnvironment environment) {
        if (instanceLock != null && instanceLock.isValid()) return true;

        String dataDir = environment.getProperty(
                "study-sync.data-dir", String.class, Path.of(System.getProperty("user.home"), ".study-sync").toString());
        try {
            Path lockPath = Path.of(dataDir).toAbsolutePath().normalize().resolve("desktop.lock");
            Files.createDirectories(lockPath.getParent());
            instanceLockChannel = FileChannel.open(lockPath,
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.WRITE);
            instanceLock = instanceLockChannel.tryLock();
            if (instanceLock == null) {
                instanceLockChannel.close();
                instanceLockChannel = null;
                return false;
            }
            return true;
        } catch (java.nio.channels.OverlappingFileLockException exception) {
            closeInstanceLock();
            return false;
        } catch (IOException exception) {
            closeInstanceLock();
            return true;
        }
    }

    private static void waitForApplication(URI dashboard) {
        long deadline = System.nanoTime() + 15_000_000_000L;
        while (System.nanoTime() < deadline && !isStudySyncRunning(dashboard)) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private static void closeInstanceLock() {
        try {
            if (instanceLock != null && instanceLock.isValid()) instanceLock.release();
        } catch (IOException ignored) {
            // The operating system releases the lock when the process exits.
        }
        try {
            if (instanceLockChannel != null && instanceLockChannel.isOpen()) instanceLockChannel.close();
        } catch (IOException ignored) {
            // The operating system releases the channel when the process exits.
        }
        instanceLock = null;
        instanceLockChannel = null;
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

    static URI dashboardUri(ConfigurableApplicationContext applicationContext) {
        int port = applicationContext.getEnvironment()
                .getProperty("local.server.port", Integer.class,
                        applicationContext.getEnvironment().getProperty("server.port", Integer.class, 3001));
        return URI.create("http://localhost:" + port + "/");
    }
}
