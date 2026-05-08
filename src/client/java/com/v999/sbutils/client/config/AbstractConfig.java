package com.v999.sbutils.client.config;

import com.v999.sbutils.client.SbutilsClient;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class AbstractConfig {
    protected abstract String getConfigName();

    private transient boolean isChanged;

    public void save() throws IOException {
        try (BufferedWriter fileOut = Files.newBufferedWriter(Path.of("config/Sbutils/" + getConfigName()))) {
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
