package com.v999.sbutils.client.config;

import com.google.gson.annotations.SerializedName;

public class ReminderEntry {
    @SerializedName("id")
    public String id = "";

    @SerializedName("message")
    public String message = "";

    @SerializedName("remind_at")
    public long remindAt = 0L;

    @SerializedName("last_reminder")
    public long lastReminder = 0L;

    public ReminderEntry() {
    }

    public ReminderEntry(String id, String message, long remindAt) {
        this.id = id;
        this.message = message;
        this.remindAt = remindAt;
    }
}
