# SBUtils

SBUtils is a client-side Fabric mod for Hypixel SkyBlock. It focuses on smaller quality-of-life tools, HUD helpers, alerts, and visual patches that are easy to toggle from the in-game ClickGUI.

Open the ClickGUI with `/sbutils` or the configured keybind.

## General

- Background blur while the SBUtils UI is open
- ClickGUI theme selector
- ClickGUI settings search bar
- Accent color selector
- Show top-right active module list
- HUD editor for movable HUD elements
- Speed Dial help link
- GitHub link

## Features

- Auto Tip
- Chat reminders for SkyBlock chores with built-in timers
- Foraging style warning
- Entrance notifier for Dungeons and Kuudra when Minecraft is unfocused
- Force toggle use on whitelisted item IDs
- Freelook
- Etherwarp Helper
- Auto conversation for safe single-option NPC prompts
- Prevent attacking Goons
- Slayer boss helper for quest, miniboss, boss spawn, and clear alerts
- 0 Ping Dungeonbreaker
- Cancel shortbow pull animation
- No command execution confirmation

## Visuals And Alerts

- Day viewer
- Dropped item glow
- Dropped item glow filters by name, item ID, or rarity
- Dropped item glow presets for rarity, Dungeons, Slayer, and Kuudra drops
- Dropped item glow rarity colors
- Compact duplicate chat messages
- Configurable compact chat counter color
- Nickname hider with custom alias
- RNG drop summary with custom sound
- Username mention sound
- Open sound folder button for `rng_music.ogg` and `user_music.ogg`
- Dynamic Island chat alerts for rare drops, username mentions, private messages, and sold auctions
- Performance HUD for FPS, ping, and TPS
- Performance HUD background color and graph options
- Always use spectator fog
- Remove suffocation screen

## Kuudra

- Auto Pearl: throws a pearl when you are holding an ender pearl while Elle's Supplies or a Ballista Fuel Cell is in slot 9.
- Supply Helper: shows Dynamic Island alerts for Elle's Supplies and Ballista Fuel Cell chat updates.

## Config

- Save config now
- Open `config/Sbutils`

The config folder contains SBUtils JSON files, synced Speed Dial contacts, and custom sound files.

## Commands

- `/sbutils` opens the ClickGUI
- `/sbutils hud` opens the HUD editor
- `/sbutils whitelist add ITEM_ID` adds a force-toggle-use item
- `/sbutils whitelist list` lists force-toggle-use items
- `/sbutils glowfilter add FILTER` adds a dropped item glow filter
- `/sbutils glowfilter preset PRESET` adds a dropped item glow preset. Presets: `rarity`, `dungeon`, `slayer`, `kuudra`
- `/sbutils glowfilter list` lists dropped item glow filters
- `/sbutils nickhider on|off|set ALIAS` controls nickname hiding
- `/sbutils resetLifeTimer` resets life saver cooldown tracking
- `/sbutils resetAutoTip` resets Auto Tip timing
- `/sbutils reminders` shows reminder status
- `/sbutils reminders test` sends the next enabled reminder in chat
- `/sbutils reminders done PRESET` marks a built-in reminder preset as completed. Presets: `daily`, `experiments`, `cakes`, `forge_minions`
- `/sbutils reminder <time> <message>` creates a custom reminder. Examples: `10m`, `2h`, `1d`, `1h30m`
- `/sbutils reminder list|remove|edit|move` manages custom reminders
- `/sbutils tps` refreshes TPS tracking
- `/sbutils autoupdates on|off|status` controls the opt-in auto updater
- `/sbutils update status` shows updater status
- `/sbutils update check` checks for GitHub releases after auto updates are enabled
- `/gy` is an alias for `/sbutils`

## Speed Dial

Speed Dial syncs Abiphone contacts by scanning the Abiphone GUI when it closes. If you have multiple contact pages, close the Abiphone on each page to sync everything.

More details are in [speeddial.md](speeddial.md).
