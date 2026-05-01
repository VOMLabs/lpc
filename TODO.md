# LPC MiniMessage X TODO List

## Completed Tasks

- Update Gradle wrapper to 8.14.4
- Update dependencies: net.kyori:adventure-platform-bukkit to 4.4.1, me.clip:placeholderapi to 2.12.2, net.kyori:adventure-text-minimessage to 4.26.1, net.luckperms:api to 5.5
- Update GitHub Actions: actions/checkout to v6, actions/setup-java to v5, gradle/actions to v6
- Add support for Minecraft 1.21.11
- Fix non-op players being able to parse PlaceholderAPI placeholders in chat messages
- Fix MiniMessage Chat-Formatter errors when using `%%` placeholders in chat-format config
- Migrate from `plugin.yml` to `paper-plugin.yml`
- Rename plugin to LPC MiniMessage X
- Refactor package structure from `de.ayont.lpc` to `com.vomlabs.lpcmmx`
- Update plugin authors to ItzzMateo and VOMLabs
- Add JSON schema validation for `config.yml` and `paper-plugin.yml`
- Integrate with Vault for economy-related placeholders
- Support MiniMessage gradient and rainbow tags in chat formats and config
- Implement chat anti-spam and swear-filtering system, including item name sanitization (e.g. `[ITEM]` will display censored names like `sh*t`).
- Expand built-in placeholders: `%player-health%` / `<player-health>`, `%player-level%` / `<player-level>`, `%world-time%` / `<world-time>`, `%player-uuid%` / `<player-uuid>`
- Implement chat mute/ignore player functionality
- Add per-world chat format support
- Add chat logging to file for server moderation
- Implement Discord webhook (optional DiscordSRV support, if no DCSRV, then default DC webhooks) integration for cross-platform chat mirroring
- Implement 10 layer encryption and hash to the plugin when compiling, make it using dependencies, etc. do not make it yourself
- Change more parts of the plugin to be async and concurrent
- Add a prefix line to the messages.yml, this prefix will be added before every message by the system automatically

## Planned Features (To Add)

- Add per-player chat format overrides via permissions or `/lpc` command
- Add chat logging to file for server moderation
- Add support for Minecraft 26.1.0+ versions
- Implement chat preview feature for Paper 1.21+
