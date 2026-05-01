package com.vomlabs.lpcmmx.api;

import com.vomlabs.lpcmmx.Main;
import com.vomlabs.lpcmmx.mute.MuteManager;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

/**
 * Type-safe public API for LPC MiniMessage X.
 * Access via LPCAPI.getInstance()
 */
public class LPCAPI {

    private static LPCAPI instance;
    private final Main plugin;

    private LPCAPI(Main plugin) {
        this.plugin = plugin;
    }

    public static void init(Main plugin) {
        if (instance == null) {
            instance = new LPCAPI(plugin);
        }
    }

    public static @NotNull LPCAPI getInstance() {
        if (instance == null) {
            throw new IllegalStateException("LPCAPI not initialized. Ensure LPC MiniMessage X is loaded.");
        }
        return instance;
    }

    // ── Mute / Ignore ────────────────────────────────

    public void mutePlayer(@NotNull Player source, @NotNull Player target) {
        plugin.getMuteManager().mutePlayer(source, target.getUniqueId());
    }

    public void unmutePlayer(@NotNull Player source, @NotNull Player target) {
        plugin.getMuteManager().unmutePlayer(source, target.getUniqueId());
    }

    public void toggleMute(@NotNull Player source, @NotNull Player target) {
        plugin.getMuteManager().toggleMute(source, target.getUniqueId());
    }

    public boolean isMuted(@NotNull Player source, @NotNull Player target) {
        return plugin.getMuteManager().isMuted(source, target.getUniqueId());
    }

    public boolean isIgnored(@NotNull Player target, @NotNull Player sender) {
        return plugin.getMuteManager().isIgnored(target, sender.getUniqueId());
    }

    public @NotNull Set<UUID> getMutedPlayers(@NotNull Player player) {
        return plugin.getMuteManager().getMutedPlayers(player);
    }

    // ── Vault Economy ──────────────────────────────────

    public boolean hasEconomy() {
        return plugin.getVaultHook().hasEconomy();
    }

    public double getBalance(@NotNull OfflinePlayer player) {
        return plugin.getVaultHook().getBalance(player);
    }

    public @NotNull String getFormattedBalance(@NotNull OfflinePlayer player) {
        return plugin.getVaultHook().getFormattedBalance(player);
    }

    // ── Chat Filter ──────────────────────────────────

    public @NotNull String filterSwearWords(@NotNull String message) {
        return plugin.getChatFilter().filterSwearWords(message);
    }

    public boolean isSpamming(@NotNull UUID playerUUID) {
        return plugin.getChatFilter().isSpamming(playerUUID);
    }
}
