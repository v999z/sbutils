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

    @SerializedName("click_gui_theme")
    public String CLICK_GUI_THEME = null;

    @SerializedName("click_gui_accent_color")
    public String CLICK_GUI_ACCENT_COLOR = "blue";

    @SerializedName("show_module_list")
    public boolean SHOW_MODULE_LIST = false;

    @SerializedName("auto_update_enabled")
    public boolean AUTO_UPDATE_ENABLED = false;

    @SerializedName("auto_update_consent_given")
    public boolean AUTO_UPDATE_CONSENT_GIVEN = false;

}
