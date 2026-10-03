package com.josev001.study_sync;

import com.josev001.study_sync.service.BackupService;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.AWTException;
import java.io.IOException;
import java.util.Objects;

@Component
public class DesktopTrayService {

    private final ConfigurableApplicationContext applicationContext;
    private final BackupService backupService;
    private final boolean desktopMode;
    private TrayIcon trayIcon;

    public DesktopTrayService(
            ConfigurableApplicationContext applicationContext,
            BackupService backupService,
            @Value("${study-sync.desktop:false}") boolean desktopMode
    ) {
        this.applicationContext = applicationContext;
        this.backupService = backupService;
        this.desktopMode = desktopMode;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void installTrayIcon() {
        if (!desktopMode || !SystemTray.isSupported()) {
            return;
        }

        try {
            Image image = ImageIO.read(Objects.requireNonNull(
                    DesktopTrayService.class.getResource("/assets/jose-victor-logo.png")));
            PopupMenu menu = new PopupMenu();

            MenuItem open = new MenuItem("Abrir Study Sync");
            open.addActionListener(event -> DesktopLauncher.openBrowser(DesktopLauncher.dashboardUri(applicationContext)));
            menu.add(open);

            MenuItem backup = new MenuItem("Fazer backup agora");
            backup.addActionListener(event -> createBackup());
            menu.add(backup);

            menu.addSeparator();
            MenuItem exit = new MenuItem("Desligar Study Sync");
            exit.addActionListener(event -> SpringApplication.exit(applicationContext, () -> 0));
            menu.add(exit);

            trayIcon = new TrayIcon(image, "Study Sync", menu);
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(event -> DesktopLauncher.openBrowser(DesktopLauncher.dashboardUri(applicationContext)));
            SystemTray.getSystemTray().add(trayIcon);
            if (DesktopLauncher.didSelectFallbackPort()) {
                trayIcon.displayMessage("Study Sync iniciou em outra porta",
                        "A porta " + DesktopLauncher.getRequestedPort() + " estava ocupada. A dashboard abriu na porta "
                                + DesktopLauncher.getSelectedPort() + ".",
                        TrayIcon.MessageType.INFO);
            }
        } catch (IOException | AWTException | RuntimeException exception) {
            System.err.println("Nao foi possivel instalar o icone da bandeja: " + exception.getMessage());
        }
    }

    private void createBackup() {
        try {
            var result = backupService.createAutomaticBackup();
            notifyUser("Backup criado", result.fileName());
        } catch (RuntimeException exception) {
            notifyUser("Falha no backup", "Nao foi possivel criar o backup local.");
        }
    }

    private void notifyUser(String title, String message) {
        if (trayIcon != null) {
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
        }
    }

    @PreDestroy
    public void removeTrayIcon() {
        if (trayIcon != null) {
            SystemTray.getSystemTray().remove(trayIcon);
        }
    }
}
