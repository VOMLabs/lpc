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

## Planned Features (To Add)

- Add per-world chat format support
- Implement chat anti-spam and filtering system
- Expand built-in placeholders: `{player-health}`, `{player-level}`, `{world-time}`, `{player-uuid}`
- Support MiniMessage gradient and rainbow tags in chat formats and config
- Add per-player chat format overrides via permissions or `/lpc` command
- Integrate with Vault for economy-related placeholders
- Add chat logging to file for server moderation
- Add support for Minecraft 26.1.0+ versions
- Add JSON schema validation for `config.yml` and `paper-plugin.yml`
- Implement chat preview feature for Paper 1.21+
- Add Discord webhook integration for cross-platform chat mirroring
- Add bStats metrics for usage tracking
- Implement chat mute/ignore player functionality
