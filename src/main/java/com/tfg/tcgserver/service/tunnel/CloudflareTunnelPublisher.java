package com.tfg.tcgserver.service.tunnel;

import com.tfg.tcgserver.persistence.firebase.FirebaseRealtimeDatabaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CloudflareTunnelPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloudflareTunnelPublisher.class);
    private static final Pattern TRY_CLOUDFLARE_URL = Pattern.compile("https://[a-zA-Z0-9.-]+\\.trycloudflare\\.com");

    private final FirebaseRealtimeDatabaseRepository firebaseRepository;
    private final boolean enabled;
    private final String cloudflaredCommand;
    private final String localUrl;
    private final String firebasePath;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Process tunnelProcess;
    private String publishedTunnelUrl;

    public CloudflareTunnelPublisher(
            FirebaseRealtimeDatabaseRepository firebaseRepository,
            @Value("${cloudflare.tunnel.enabled:false}") boolean enabled,
            @Value("${cloudflare.tunnel.command:cloudflared}") String cloudflaredCommand,
            @Value("${cloudflare.tunnel.local-url:http://localhost:${server.port}}") String localUrl,
            @Value("${cloudflare.tunnel.firebase-path:config/backend}") String firebasePath
    ) {
        this.firebaseRepository = firebaseRepository;
        this.enabled = enabled;
        this.cloudflaredCommand = cloudflaredCommand;
        this.localUrl = localUrl;
        this.firebasePath = firebasePath;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void publishTunnelUrl() {
        if (!enabled) {
            return;
        }

        killStaleCloudflaredProcesses();
        executorService.submit(this::startTunnelAndPublishUrl);
        Runtime.getRuntime().addShutdownHook(new Thread(this::stopTunnel));
    }

    /**
     * Si una ejecucion anterior murio sin pasar por el shutdown hook (kill forzado,
     * cierre de consola, debug stop), el proceso cloudflared anterior queda huerfano
     * ocupando el tunel. Lo matamos antes de lanzar uno nuevo.
     */
    private void killStaleCloudflaredProcesses() {
        String processName = extractProcessName(cloudflaredCommand);

        try {
            ProcessBuilder killer = isWindows()
                    ? new ProcessBuilder("taskkill", "/F", "/IM", processName)
                    : new ProcessBuilder("pkill", "-f", processName);

            killer.redirectErrorStream(true);
            Process process = killer.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                while (reader.readLine() != null) {
                    // descarta salida del comando de limpieza
                }
            }

            process.waitFor();
        } catch (IOException | InterruptedException exception) {
            LOGGER.debug("No se pudo limpiar procesos cloudflared previos (puede que no hubiera ninguno)", exception);
        }
    }

    private String extractProcessName(String command) {
        String fileName = Paths.get(command).getFileName().toString();

        if (isWindows() && !fileName.toLowerCase().endsWith(".exe")) {
            return fileName + ".exe";
        }

        return fileName;
    }

    private boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    private void startTunnelAndPublishUrl() {
        ProcessBuilder processBuilder = new ProcessBuilder(
                cloudflaredCommand,
                "tunnel",
                "--url",
                localUrl
        );
        processBuilder.redirectErrorStream(true);

        try {
            tunnelProcess = processBuilder.start();
            LOGGER.info("Cloudflare Tunnel iniciado para {}", localUrl);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(tunnelProcess.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    publishFirstUrlInLine(line);
                }
            }
        } catch (IOException exception) {
            LOGGER.warn("No se pudo iniciar cloudflared. Instala cloudflared o desactiva cloudflare.tunnel.enabled.", exception);
        }
    }

    private void publishFirstUrlInLine(String line) {
        Matcher matcher = TRY_CLOUDFLARE_URL.matcher(line);
        if (!matcher.find()) {
            return;
        }

        String tunnelUrl = matcher.group();
        if (publishedTunnelUrl != null) {
            return;
        }
        publishedTunnelUrl = tunnelUrl;

        Map<String, Object> backendConfig = Map.of(
                "baseUrl", tunnelUrl,
                "generatedAt", Instant.now().toString(),
                "source", "cloudflare-tunnel",
                "localUrl", localUrl
        );

        firebaseRepository.save(firebasePath, backendConfig)
                .thenRun(() -> {
                    LOGGER.info("URL publica publicada en Firebase: {}", tunnelUrl);
                    System.out.println("Cloudflare Tunnel URL generada: " + tunnelUrl);
                })
                .exceptionally(exception -> {
                    LOGGER.warn("No se pudo publicar la URL publica en Firebase", exception);
                    return null;
                });
    }

    private void stopTunnel() {
        executorService.shutdownNow();

        if (tunnelProcess != null && tunnelProcess.isAlive()) {
            tunnelProcess.destroy();
            tunnelProcess.descendants().forEach(ProcessHandle::destroy);
        }
    }
}
