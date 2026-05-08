package com.v999.sbutils.client.feature;

import com.google.gson.annotations.SerializedName;
import com.v999.sbutils.client.SbutilsClient;
import com.v999.sbutils.client.config.ConfigManager;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Arrays;
import java.util.jar.JarFile;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public final class AutoUpdater {
    private static final long CHECK_INTERVAL_MS = 15L * 60L * 1000L;
    private static final AtomicBoolean CHECKING = new AtomicBoolean();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    private static final String PENDING_UPDATE_SUFFIX = ".sbutils-update";
    private static final String UPDATE_REPO = "v999z/sbutils";
    private static final String EXPECTED_MOD_ID = "sbutils";
    private static final String EXPECTED_ASSET_PREFIX = "sbutils-";

    private static volatile long lastCheckStartedMs;
    private static volatile String statusLine = "Ready. Use /sbutils update status.";

    private AutoUpdater() {
    }

    public static void init() {
        if (ConfigManager.GENERAL.AUTO_UPDATE_ENABLED) {
            checkForUpdatesAsync(false);
        } else {
            statusLine = "Auto updater is disabled.";
        }
    }

    public static void onClientTick(Minecraft client) {
        if (!ConfigManager.GENERAL.AUTO_UPDATE_ENABLED || CHECKING.get()) {
            return;
        }
        long now = Util.getMillis();
        if (lastCheckStartedMs == 0L || now - lastCheckStartedMs >= CHECK_INTERVAL_MS) {
            checkForUpdatesAsync(false);
        }
    }

    public static void onClientStopping() {
        if (!ConfigManager.GENERAL.AUTO_UPDATE_ENABLED) {
            return;
        }
        trySchedulePendingInstall();
    }

    public static String getStatusLine() {
        return statusLine;
    }

    public static void setEnabledState(boolean enabled) {
        statusLine = enabled ? "Auto updater enabled." : "Auto updater disabled.";
        if (!enabled) {
            clearPendingUpdate();
        }
    }

    public static void clearPendingUpdate() {
        resolveCurrentJarPath()
                .map(AutoUpdater::pendingPathFor)
                .ifPresent(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        SbutilsClient.LOGGER.warn("[Sbutils AutoUpdater] Failed to clear pending update {}", path, e);
                    }
                });
    }

    public static void checkForUpdatesAsync(boolean manual) {
        if (!ConfigManager.GENERAL.AUTO_UPDATE_ENABLED && !manual) {
            statusLine = "Auto updater is disabled.";
            return;
        }
        if (!CHECKING.compareAndSet(false, true)) {
            statusLine = "Already checking GitHub for updates...";
            return;
        }
        lastCheckStartedMs = Util.getMillis();
        statusLine = "Checking GitHub releases...";

        Thread.startVirtualThread(() -> {
            try {
                performCheck(manual);
            } catch (Exception e) {
                statusLine = "Update check failed. See log for details.";
                SbutilsClient.LOGGER.error("[Sbutils AutoUpdater] Failed to check for updates", e);
            } finally {
                CHECKING.set(false);
            }
        });
    }

    private static void performCheck(boolean manual) throws IOException, InterruptedException {
        String repo = UPDATE_REPO;

        HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.github.com/repos/" + repo + "/releases/latest"))
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .header("User-Agent", "SBUtils AutoUpdater")
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build();

        HttpResponse<String> releaseResponse = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        if (releaseResponse.statusCode() != 200) {
            statusLine = "GitHub responded with HTTP " + releaseResponse.statusCode() + '.';
            return;
        }

        GithubRelease release = SbutilsClient.GSON.fromJson(releaseResponse.body(), GithubRelease.class);
        if (release == null || release.assets == null || release.assets.length == 0) {
            statusLine = "No downloadable release jar found yet.";
            return;
        }

        Optional<GithubAsset> chosenAsset = Arrays.stream(release.assets)
                .filter(asset -> asset != null && asset.browserDownloadUrl != null && asset.name != null)
                .filter(asset -> asset.name.endsWith(".jar"))
                .filter(asset -> !asset.name.contains("-sources"))
                .filter(asset -> asset.name.startsWith(EXPECTED_ASSET_PREFIX))
                .findFirst();
        if (chosenAsset.isEmpty()) {
            statusLine = "Release exists, but there is no jar asset to download.";
            return;
        }

        String currentVersion = FabricLoader.getInstance().getModContainer("sbutils")
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
        String latestVersion = normalizeVersion(release.tagName);
        String normalizedCurrentVersion = normalizeVersion(currentVersion);
        GithubAsset asset = chosenAsset.get();

        Optional<Path> currentJar = resolveCurrentJarPath();
        Path destination;
        if (currentJar.isPresent()) {
            destination = pendingPathFor(currentJar.get());
        } else {
            Path updateDir = Path.of("config", "Sbutils", "updates");
            Files.createDirectories(updateDir);
            destination = updateDir.resolve(asset.name);
        }

        if (latestVersion.equals(normalizedCurrentVersion)) {
            currentJar.map(AutoUpdater::pendingPathFor).ifPresent(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    SbutilsClient.LOGGER.debug("[Sbutils AutoUpdater] Failed to clear stale pending update {}", path, e);
                }
            });
            statusLine = "You're up to date on " + currentVersion + ".";
            return;
        }

        if (Files.exists(destination) && (asset.size <= 0 || Files.size(destination) == asset.size)) {
            statusLine = currentJar.isPresent()
                    ? "Update " + release.tagName + " is ready. Restart the game to apply it."
                    : "Update " + release.tagName + " is already downloaded to config/Sbutils/updates.";
            return;
        }

        if (currentJar.isPresent()) {
            cleanupSiblingPending(currentJar.get(), destination);
        } else {
            cleanupFallbackDownloads(destination.getParent(), destination);
        }

        HttpRequest downloadRequest = HttpRequest.newBuilder(URI.create(asset.browserDownloadUrl))
                .header("User-Agent", "SBUtils AutoUpdater")
                .timeout(Duration.ofSeconds(60))
                .GET()
                .build();
        HttpResponse<InputStream> downloadResponse = HTTP.send(downloadRequest, HttpResponse.BodyHandlers.ofInputStream());
        if (downloadResponse.statusCode() != 200) {
            statusLine = "Download failed with HTTP " + downloadResponse.statusCode() + '.';
            return;
        }

        Path tempFile = destination.resolveSibling(destination.getFileName() + ".part");
        try (InputStream inputStream = downloadResponse.body()) {
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
        }
        if (!verifyDownloadedJar(tempFile)) {
            Files.deleteIfExists(tempFile);
            statusLine = "Downloaded file failed verification.";
            return;
        }
        try {
            Files.move(tempFile, destination, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException ignored) {
            Files.move(tempFile, destination, StandardCopyOption.REPLACE_EXISTING);
        }

        statusLine = currentJar.isPresent()
                ? "Downloaded " + release.tagName + ". Restart the game to install it automatically."
                : "Downloaded " + release.tagName + " to config/Sbutils/updates.";
        SbutilsClient.LOGGER.info("[Sbutils AutoUpdater] Downloaded {} to {}", asset.name, destination.toAbsolutePath());
    }

    private static Optional<Path> resolveCurrentJarPath() {
        try {
            Path path = Path.of(SbutilsClient.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toAbsolutePath();
            if (Files.isRegularFile(path) && path.getFileName().toString().endsWith(".jar")) {
                return Optional.of(path);
            }
        } catch (Exception e) {
            SbutilsClient.LOGGER.debug("[Sbutils AutoUpdater] Unable to resolve current jar path", e);
        }
        return Optional.empty();
    }

    private static Path pendingPathFor(Path currentJar) {
        return currentJar.resolveSibling(currentJar.getFileName().toString() + PENDING_UPDATE_SUFFIX);
    }

    private static void trySchedulePendingInstall() {
        Optional<Path> currentJar = resolveCurrentJarPath();
        if (currentJar.isEmpty()) {
            return;
        }
        Path stagedFile = pendingPathFor(currentJar.get());
        if (!Files.exists(stagedFile)) {
            return;
        }
        try {
            launchInstaller(currentJar.get(), stagedFile);
            statusLine = "Update is queued and will be installed as the game closes.";
        } catch (IOException e) {
            statusLine = "Update downloaded, but failed to schedule install.";
            SbutilsClient.LOGGER.error("[Sbutils AutoUpdater] Failed to launch installer", e);
        }
    }

    private static void launchInstaller(Path currentJar, Path stagedFile) throws IOException {
        String javaExe = Path.of(
                System.getProperty("java.home"),
                "bin",
                System.getProperty("os.name", "").toLowerCase().contains("win") ? "javaw.exe" : "java"
        ).toString();

        new ProcessBuilder(
                javaExe,
                "-cp",
                currentJar.toAbsolutePath().toString(),
                "com.v999.sbutils.client.feature.UpdateInstaller",
                Long.toString(ProcessHandle.current().pid()),
                stagedFile.toAbsolutePath().toString(),
                currentJar.toAbsolutePath().toString()
        )
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start();
    }

    private static boolean verifyDownloadedJar(Path jarPath) {
        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            var entry = jarFile.getEntry("fabric.mod.json");
            if (entry == null) {
                return false;
            }

            try (InputStream in = jarFile.getInputStream(entry)) {
                String json = new String(in.readAllBytes());
                return json.contains("\"id\": \"" + EXPECTED_MOD_ID + "\"")
                        || json.contains("\"id\":\"" + EXPECTED_MOD_ID + "\"");
            }
        } catch (Exception e) {
            SbutilsClient.LOGGER.warn("[Sbutils AutoUpdater] Downloaded jar failed verification", e);
            return false;
        }
    }

    private static void cleanupSiblingPending(Path currentJar, Path keepFile) {
        Path siblingDir = currentJar.getParent();
        String currentJarName = currentJar.getFileName().toString();
        try (var files = Files.list(siblingDir)) {
            files.filter(path -> !path.equals(keepFile))
                    .filter(path -> path.getFileName().toString().startsWith(currentJarName) && path.getFileName().toString().contains(PENDING_UPDATE_SUFFIX))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            SbutilsClient.LOGGER.warn("[Sbutils AutoUpdater] Failed to delete old pending update {}", path, e);
                        }
                    });
        } catch (IOException e) {
            SbutilsClient.LOGGER.warn("[Sbutils AutoUpdater] Failed to clean pending updates near {}", currentJar, e);
        }
    }

    private static void cleanupFallbackDownloads(Path updateDir, Path keepFile) {
        try (var files = Files.list(updateDir)) {
            files.filter(path -> !path.equals(keepFile))
                    .sorted(Comparator.comparing(Path::toString))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            SbutilsClient.LOGGER.warn("[Sbutils AutoUpdater] Failed to delete old update file {}", path, e);
                        }
                    });
        } catch (IOException e) {
            SbutilsClient.LOGGER.warn("[Sbutils AutoUpdater] Failed to clean update directory {}", updateDir, e);
        }
    }

    private static String normalizeVersion(String version) {
        if (version == null) {
            return "";
        }
        String normalized = version.trim();
        if (normalized.startsWith("v") || normalized.startsWith("V")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }

    private static final class GithubRelease {
        @SerializedName("tag_name")
        String tagName;
        GithubAsset[] assets;
    }

    private static final class GithubAsset {
        String name;
        long size;
        @SerializedName("browser_download_url")
        String browserDownloadUrl;
    }
}