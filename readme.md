![LPC Banner](https://cdn.varilx.de/raw/fwtRZS.png)

<p align="center">
  <a href="https://modrinth.com/plugin/lpc-chat">
    <img src="https://raw.githubusercontent.com/vLuckyyy/badges/main/avaiable-on-modrinth.svg" alt="Available on Modrinth" />
  </a>
</p>

<p align="center">
  <a href="https://discord.gg/ZPyb9g6Gs4">
    <img src="https://img.shields.io/discord/1322873747535040512" alt="Discord">
  </a>
  <a href="https://github.com/VOMLabs/lpc/actions/workflows/publish.yml">
    <img src="https://img.shields.io/github/actions/workflow/status/VOMLabs/lpc/publish.yml?branch=master" alt="Build Status">
  </a>
  <a href="https://github.com/VOMLabs/lpc/releases">
    <img src="https://img.shields.io/github/v/release/VOMLabs/lpc" alt="Latest Release">
  </a>
</p>

# LPC MiniMessage X ✨  
**A modern chat formatting plugin with full MiniMessage support, powered by LuckPerms**

> Built on [MiniMessage](https://docs.advntr.dev/minimessage/format.html) with LuckPerms metadata, group/track formats, PlaceholderAPI, Vault economy, and rich gradient/rainbow chat styles!

---

## 🔧 Requirements

- [LuckPerms](https://www.spigotmc.org/resources/luckperms.28140/) *(Required)* – Permissions plugin  
- [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) *(Optional)* – Additional placeholders  
- [Vault](https://www.spigotmc.org/resources/vault.34315/) *(Optional)* – Economy integration (requires an economy plugin like EssentialsX)

---

## ✅ Features

- Full [MiniMessage](https://docs.advntr.dev/minimessage/format.html) formatting support
- **Gradient & Rainbow tags** – `<gradient:red:blue>` and `<rainbow>` in chat formats
- **Chat anti-spam & swear filtering** – configurable filter with item name filtering
- **Player ignore system** – mute players for yourself, their messages appear grayed out
- **Per-world chat formats** – world-specific, group, and track-based formats
- **10-layer encryption & hash** – Bouncy Castle encryption for config values
- **Async & concurrent** – non-blocking database, logging, and message operations
- **C++ Native API** – high-performance JNI implementation (falls back to Java)
- **Discord webhook integration** – DiscordSRV support + direct webhooks
- **Chat logging** – daily rotating log files for moderation
- **Private messages (MSG)** – send messages to players with toggle support
- **FastStats metrics** – usage tracking for plugin improvements
- Group and track-specific chat formats
- Optional support for PlaceholderAPI
- **Vault economy integration** – `%balance%` / `<balance>` and `%balance-formatted%` / `<balance-formatted>` placeholders
- Supports `[ITEM]` placeholder with hover info (with swear filtering)
- **10-layer encryption** – Bouncy Castle AES/XOR/SHA-256/SHA-3/SHA-512
- Customizable messages in `messages.yml` with prefix support
- JSON Schema validation for `config.yml` and `paper-plugin.yml`
- Modern Paper plugin format (`paper-plugin.yml`)
- **Explicit permission system** – `lpc.*`, `lpc.admin`, `lpc.mute.*`, `lpc.msg.*`, etc.

---

## 🧑‍💼 Permissions

### Main Permission Nodes

| Permission Node       | Description | Default |
|----------------------|-------------|---------|
| `lpc.*`              | All LPC permissions | op      |
| `lpc.admin`          | Admin access to all features | op      |

### Chat & Display

| Permission Node       | Description | Default |
|----------------------|-------------|---------|
| `lpc.chatcolor`     | Allows using MiniMessage color codes in chat | false   |
| `lpc.itemplaceholder` | Enables the `[ITEM]` placeholder in chat | false   |

### Mute System

| Permission Node       | Description | Default |
|----------------------|-------------|---------|
| `lpc.mute.*`         | All mute-related permissions | op      |
| `lpc.mute`           | Mute/unmute players for yourself | false   |
| `lpc.mute.toggle`    | Toggle mute status of a player | false   |
| `lpc.mute.list`       | View your muted players list | true    |

### Admin Commands

| Command               | Permission Node | Description |
|-----------------------|---------------|-------------|
| `/lpc reload`        | `lpc.reload`   | Reloads the configuration |
| `/lpc mute <player>` | `lpc.mute`    | Mute/unmute a player for yourself |

---

## ⚙️ Configuration (`config.yml`)

```yaml
# LPC Configuration

# Placeholders (supports both %placeholder% and <placeholder> formats):
# %message% / <message>, %name% / <name>, %displayname% / <displayname>
# %world% / <world>, %prefix% / <prefix>, %suffix% / <suffix>
# %prefixes% / <prefixes>, %suffixes% / <suffixes>
# %username-color% / <username-color>, %message-color% / <message-color>
# %balance% / <balance>, %balance-formatted% / <balance-formatted> (requires Vault)

# MiniMessage Gradient and Rainbow support:
# <gradient:color1:color2>text</gradient>
# <rainbow>text</rainbow>

chat-format: "<prefix><name><dark_gray> »<reset> <message>"

# Format per group (optional)
group-formats:
#  admin: "<gradient:red:gold><prefix><name></gradient><dark_gray> »<reset> <message>"
#  default: "<prefix><name><dark_gray> »<reset> <message>"

# Format per track (optional)
track-formats:
#  staff_track: "<rainbow><prefix><name></rainbow><dark_gray> »<reset> <message>"

# Enable the [ITEM] placeholder
use-item-placeholder: true

# Reload message
reload-message: "<green>Reloaded LPC Configuration!"
```

---

## 📋 Available Placeholders

| Placeholder             | Description |
|-------------------------|-------------|
| `%message%` / `<message>` | The chat message |
| `%name%` / `<name>`       | Player's name |
| `%displayname%` / `<displayname>` | Display name / nickname |
| `%world%` / `<world>`     | Player's current world |
| `%prefix%` / `<prefix>`   | Highest priority prefix |
| `%suffix%` / `<suffix>`   | Highest priority suffix |
| `%prefixes%` / `<prefixes>` | Sorted list of all prefixes |
| `%suffixes%` / `<suffixes>` | Sorted list of all suffixes |
| `%username-color%` / `<username-color>` | Username color from LuckPerms meta |
| `%message-color%` / `<message-color>` | Message color from LuckPerms meta |
| `%balance%` / `<balance>` | Player's economy balance (requires Vault) |
| `%balance-formatted%` / `<balance-formatted>` | Player's formatted economy balance (requires Vault) |
| `%player-health%` / `<player-health>` | Player's current health |
| `%player-level%` / `<player-level>` | Player's current level |
| `%world-time%` / `<world-time>` | Current world time |
| `%player-uuid%` / `<player-uuid>` | Player's UUID |

> ℹ️ **Important:** All color values (prefix, suffix, etc.) must be in **MiniMessage format** – no legacy codes (`&a`, `§b`, etc.)

---

## 🖼️ Previews

**Chat Format Example**  
![Chatformat](https://cdn.modrinth.com/data/cached_images/690d3848aefb13b4088df4e388218347383eef86.png)

**[ITEM] Placeholder Example**  
![Item Placeholder](https://cdn.modrinth.com/data/cached_images/5e95c782f9e06878f56633e45ac4b465e540ac97.png)

---

## 🚀 Installation

1. Stop your server  
2. Place the `LPC-MiniMessage.jar` into your `/plugins` folder  
3. Start the server to generate configuration files  
4. Edit the `config.yml` to your liking  
5. Use `/lpc reload` to apply your changes ✅

---

## 📌 Notes

- **Not affiliated with LuckPerms** – Please do not contact the LuckPerms author for support!
- Requires [Paper](https://papermc.io/) or a Paper-based fork (Purpur, etc.)
- Legacy version available at: [GitHub Legacy LPC](https://github.com/wikmor/LPC)
- Report issues at: [GitHub Issues](https://github.com/VOMLabs/lpc/issues)

---

## 📄 License

This project is licensed under the MIT License – see the LICENSE file for details.
