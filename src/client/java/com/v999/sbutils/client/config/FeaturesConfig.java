package com.v999.sbutils.client.config;

import com.google.gson.annotations.SerializedName;

public class FeaturesConfig extends AbstractConfig {
    static String CONFIG_NAME = "features.json";

    @Override
    protected String getConfigName() {
        return CONFIG_NAME;
    }

    @SerializedName("enable_day_viewer")
    public boolean ENABLE_DAY_VIEWER = false;

    @SerializedName("enable_compact_duplicate_messages")
    public boolean ENABLE_COMPACT_DUPLICATE_MESSAGES = true;

    @SerializedName("compact_duplicate_messages_color")
    public String COMPACT_DUPLICATE_MESSAGES_COLOR = "accent";

    @SerializedName("enable_auto_tip")
    public boolean ENABLE_AUTO_TIP = false;

    @SerializedName("enable_reminders")
    public boolean ENABLE_REMINDERS = false;

    @SerializedName("reminders_skyblock_only")
    public boolean REMINDERS_SKYBLOCK_ONLY = true;

    @SerializedName("remind_daily_tasks")
    public boolean REMIND_DAILY_TASKS = true;

    @SerializedName("remind_experiment_table")
    public boolean REMIND_EXPERIMENT_TABLE = true;

    @SerializedName("remind_cakes")
    public boolean REMIND_CAKES = true;

    @SerializedName("remind_forge_and_minions")
    public boolean REMIND_FORGE_AND_MINIONS = true;

    @SerializedName("reminders_last_daily_tasks")
    public long REMINDERS_LAST_DAILY_TASKS = 0L;

    @SerializedName("reminders_last_experiment_table")
    public long REMINDERS_LAST_EXPERIMENT_TABLE = 0L;

    @SerializedName("reminders_last_cakes")
    public long REMINDERS_LAST_CAKES = 0L;

    @SerializedName("reminders_last_forge_and_minions")
    public long REMINDERS_LAST_FORGE_AND_MINIONS = 0L;

    @SerializedName("custom_reminders")
    public java.util.ArrayList<ReminderEntry> CUSTOM_REMINDERS = new java.util.ArrayList<>();

    @SerializedName("enable_rng_drop_summary")
    public boolean ENABLE_RNG_DROP_SUMMARY = true;

    @SerializedName("enable_foraging_style_warning")
    public boolean ENABLE_FORAGING_STYLE_WARNING = true;

    @SerializedName("enable_entrance_notifier")
    public boolean ENABLE_ENTRANCE_NOTIFIER = true;

    @SerializedName("enable_force_toggle_use")
    public boolean ENABLE_FORCE_TOGGLE_USE = false;

    @SerializedName("enable_etherwarp_helper")
    public boolean ENABLE_ETHERWARP_HELPER = false;

    @SerializedName("enable_kuudra_auto_pearl")
    public boolean ENABLE_KUUDRA_AUTO_PEARL = false;

    @SerializedName("enable_slayer_boss_helper")
    public boolean ENABLE_SLAYER_BOSS_HELPER = true;

    @SerializedName("enable_kuudra_supply_helper")
    public boolean ENABLE_KUUDRA_SUPPLY_HELPER = true;

    @SerializedName("force_toggle_use_whitelist")
    public java.util.ArrayList<String> FORCE_TOGGLE_USE_WHITELIST = new java.util.ArrayList<>();


    @SerializedName("enable_free_look")
    public boolean ENABLE_FREELOOK = false;

    @SerializedName("enable_auto_conversation")
    public boolean ENABLE_AUTO_CONVERSATION = false;

    @SerializedName("enable_prevent_attacking_on_goons")
    public boolean ENABLE_PREVENT_ATTACKING_ON_GOONS = false;

    @SerializedName("enable_dropped_item_glow")
    public boolean ENABLE_DROPPED_ITEM_GLOW = false;

    @SerializedName("dropped_item_glow_only_matching")
    public boolean DROPPED_ITEM_GLOW_ONLY_MATCHING = false;

    @SerializedName("dropped_item_glow_rarity_colors")
    public boolean DROPPED_ITEM_GLOW_RARITY_COLORS = true;

    @SerializedName("dropped_item_glow_filters")
    public java.util.ArrayList<String> DROPPED_ITEM_GLOW_FILTERS = new java.util.ArrayList<>();

    @SerializedName("enable_nickname_hider")
    public boolean ENABLE_NICKNAME_HIDER = false;

    @SerializedName("nickname_hider_alias")
    public String NICKNAME_HIDER_ALIAS = "SBUtils User";

    @SerializedName("enable_username_mention_sound")
    public boolean ENABLE_USERNAME_MENTION_SOUND = false;

    @SerializedName("enable_dynamic_island_chat_alerts")
    public boolean ENABLE_DYNAMIC_ISLAND_CHAT_ALERTS = true;

    @SerializedName("enable_performance_hud")
    public boolean ENABLE_PERFORMANCE_HUD = false;

    @SerializedName("show_performance_fps")
    public boolean SHOW_PERFORMANCE_FPS = true;

    @SerializedName("show_performance_ping")
    public boolean SHOW_PERFORMANCE_PING = true;

    @SerializedName("show_performance_tps")
    public boolean SHOW_PERFORMANCE_TPS = true;

    @SerializedName("performance_hud_transparent_background")
    public boolean PERFORMANCE_HUD_TRANSPARENT_BACKGROUND = false;

    @SerializedName("performance_hud_background_color")
    public String PERFORMANCE_HUD_BACKGROUND_COLOR = "legacy";

    @SerializedName("performance_hud_graph_enabled")
    public boolean PERFORMANCE_HUD_GRAPH_ENABLED = false;

    @SerializedName("performance_hud_graph_metric")
    public String PERFORMANCE_HUD_GRAPH_METRIC = "fps";

    @SerializedName("performance_hud_x")
    public float PERFORMANCE_HUD_X = 8F;

    @SerializedName("performance_hud_y")
    public float PERFORMANCE_HUD_Y = 8F;

    @SerializedName("dynamic_island_x")
    public float DYNAMIC_ISLAND_X = -1F;

    @SerializedName("dynamic_island_y")
    public float DYNAMIC_ISLAND_Y = -1F;

}
