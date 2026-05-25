package com.v999.sbutils.client.config;

import com.v999.sbutils.client.SbutilsClient;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;

public abstract class AbstractConfig {
    protected abstract String getConfigName();

    private transient boolean isChanged;

    public void save() throws IOException {
        Files.createDirectories(ConfigManager.CONFIG_DIR);
        try (BufferedWriter fileOut = Files.newBufferedWriter(ConfigManager.CONFIG_DIR.resolve(getConfigName()))) {
            SbutilsClient.GSON.toJson(this, fileOut);
        }
    }

    public void markAsChanged() {
        isChanged = true;
    }

    void processChanges() {
        if (isChanged) {
            isChanged = false;
            try {
                save();
            } catch (IOException e) {
                SbutilsClient.LOGGER.error("[Sbutils Config Manager] Failed to save {}", getConfigName(), e);
            }
        }
    }
}
