package com.v999.sbutils.client.config;

import com.google.gson.annotations.SerializedName;

public class GeneralConfig extends AbstractConfig {
    static String CONFIG_NAME = "general.json";

    @Override
    protected String getConfigName() {
        return CONFIG_NAME;
    }

    @SerializedName("click_gui_blur")
    public boolean CLICK_GUI_BLUR = true;

    @SerializedName("click_gui_light_mode")
    public boolean CLICK_GUI_LIGHT_MODE = false;

    @SerializedName("click_gui_accent_color")
    public String CLICK_GUI_ACCENT_COLOR = "blue";

    @SerializedName("auto_update_enabled")
    public boolean AUTO_UPDATE_ENABLED = true;

}
