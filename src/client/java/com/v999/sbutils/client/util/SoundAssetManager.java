package com.v999.sbutils.client.util;

import com.v999.sbutils.client.SbutilsClient;
import net.minecraft.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class SoundAssetManager {
    public static final Path CONFIG_DIR = Path.of("config", "Sbutils");

    private SoundAssetManager() {
    }

    public static void ensureDefaultSounds() {
        ensureFile("/assets/sbutils/default_sounds/rng_music.ogg", CONFIG_DIR.resolve("rng_music.ogg"));
        ensureFile("/assets/sbutils/default_sounds/user_music.ogg", CONFIG_DIR.resolve("user_music.ogg"));
    }

    public static void openSoundFolder() {
        try {
            Files.createDirectories(CONFIG_DIR);
            Util.getPlatform().openPath(CONFIG_DIR.toAbsolutePath());
        } catch (IOException e) {
            SbutilsClient.LOGGER.warn("[Sbutils] Failed to open sound folder {}", CONFIG_DIR, e);
        }
    }

    private static void ensureFile(String resourcePath, Path destination) {
        try {
            Files.createDirectories(destination.getParent());
            if (Files.exists(destination) && Files.size(destination) > 0L) {
                return;
            }

            try (InputStream in = SoundAssetManager.class.getResourceAsStream(resourcePath)) {
                if (in == null) {
                    SbutilsClient.LOGGER.warn("[Sbutils] Missing bundled sound resource {}", resourcePath);
                    return;
                }
                Path tempFile = destination.resolveSibling(destination.getFileName() + ".part");
                Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
                try {
                    Files.move(tempFile, destination, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                } catch (IOException ignored) {
                    Files.move(tempFile, destination, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (IOException e) {
            SbutilsClient.LOGGER.warn("[Sbutils] Failed to copy bundled sound {} to {}", resourcePath, destination, e);
        }
    }
}
