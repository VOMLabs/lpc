package com.vomlabs.lpcmmx.listener;

import com.vomlabs.lpcmmx.Main;
import com.vomlabs.lpcmmx.filter.ChatFilter;
import com.vomlabs.lpcmmx.mute.MuteManager;
import com.vomlabs.lpcmmx.emoji.EmojiManager;
import com.vomlabs.lpcmmx.suspend.ChatSuspendManager;
import com.vomlabs.lpcmmx.translation.TranslationManager;
import com.vomlabs.lpcmmx.url.URLFilter;
import com.vomlabs.lpcmmx.discord.DiscordWebhook;
import com.vomlabs.lpcmmx.logging.ChatLogger;
import com.vomlabs.lpcmmx.renderer.LPCChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import static java.util.regex.Pattern.*;

public class AsyncChatListener implements Listener {

    private final Main plugin;
    private final LPCChatRenderer lpcChatRenderer;

    public AsyncChatListener(Main plugin) {
        this.plugin = plugin;
        this.lpcChatRenderer = new LPCChatRenderer(plugin);
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        final Player player = event.getPlayer();
        final ChatFilter filter = plugin.getChatFilter();
        final MuteManager muteManager = plugin.getMuteManager();
        final ChatSuspendManager suspendManager = plugin.getSuspendManager();
        final TranslationManager translationManager = plugin.getTranslationManager();
        final URLFilter urlFilter = plugin.getURLFilter();
        final DiscordWebhook discordWebhook = plugin.getDiscordWebhook();
        final ChatLogger chatLogger = plugin.getChatLogger();

        // Check if player is suspended
        if (suspendManager.isSuspended(player.getUniqueId())) {
            ChatSuspendManager.SuspensionInfo info = suspendManager.getSuspensionInfo(player.getUniqueId());
            String reason = info != null ? info.reason : "No reason";
            player.sendMessage(plugin.getMessageManager().getComponent("suspend.chat-blocked", "reason", reason));
            event.setCancelled(true);
            return;
        }

        if (filter.isAntiSpamEnabled() && filter.isSpamming(player.getUniqueId())) {
            player.sendMessage(plugin.getMessageManager().getComponent("chat.spam-warning"));
            event.setCancelled(true);
            return;
        }

        String plainMessage = PlainTextComponentSerializer.plainText().serialize(event.message());

        if (filter.isSwearFilterEnabled()) {
            String filtered = filter.filterSwearWords(plainMessage);
            if (!plainMessage.equals(filtered)) {
                event.message(Component.text(filtered));
                plainMessage = filtered;
            }
        }

        // Process emojis
        EmojiManager emojiManager = plugin.getEmojiManager();
        Component emojiProcessed = emojiManager.processMessage(plainMessage, player);
        if (emojiProcessed != null) {
            event.message(emojiProcessed);
            plainMessage = PlainTextComponentSerializer.plainText().serialize(event.message());
        }

        // Process URLs with clickable links
        if (urlFilter.containsURL(plainMessage)) {
            if (plugin.getConfig().getBoolean("url-filter.block-messages", false)) {
                player.sendMessage(plugin.getMessageManager().getComponent("url.blocked"));
                event.setCancelled(true);
                return;
            }
            Component urlProcessed = urlFilter.processMessage(plainMessage);
            if (urlProcessed != null) {
                event.message(urlProcessed);
            }
        }

        // Log the message
        chatLogger.logMessage(player, event.message());

        // Apply translation if enabled
        if (translationManager.isTranslationEnabled(player.getUniqueId())) {
            String targetLang = translationManager.getLanguage(player.getUniqueId());
            translationManager.translate(plainMessage, targetLang).thenAccept(translated -> {
                // Store translated message for Discord/spectators
                player.setMetadata("translated_msg", new org.bukkit.metadata.FixedMetadataValue(plugin, translated));
            });
        }

        // Send to Discord webhook
        if (discordWebhook != null && discordWebhook.isEnabled()) {
            discordWebhook.sendChatMessage(event.message(), player.getName(), null);
        }

        // Check if any viewer has muted this player
        event.viewers().removeIf(viewer -> {
            if (viewer instanceof Player) {
                return muteManager.isIgnored(player, ((Player) viewer).getUniqueId());
            }
            return false;
        });

        if (!plugin.getConfig().getBoolean("use-item-placeholder", false) || !player.hasPermission("lpc.itemplaceholder")) {
            return;
        }

        final ItemStack item = player.getInventory().getItemInMainHand();
        Component displayName = item.getItemMeta() != null && item.getItemMeta().hasDisplayName()
                ? item.getItemMeta().displayName()
                : Component.text(item.getType().toString().toLowerCase().replace("_", " "));
        if (item.getType().equals(Material.AIR) || displayName == null) {
            return;
        }

        if (filter.isSwearFilterEnabled() && filter.filterItemNames) {
            String filteredName = filter.filterItemName(PlainTextComponentSerializer.plainText().serialize(displayName));
            displayName = Component.text(filteredName);
        }

        final Component finalDisplayName = displayName;

        event.renderer((source, sourceDisplayName, message, viewer) -> {
            Component rendered = lpcChatRenderer.render(source, sourceDisplayName, message, viewer)
                    .replaceText(TextReplacementConfig.builder().match(compile("\\[item]", CASE_INSENSITIVE))
                            .replacement(finalDisplayName.hoverEvent(item)).build());

            if (viewer instanceof Player && muteManager.isIgnored(player, ((Player) viewer).getUniqueId())) {
                return Component.text("§7[Ignored] ").append(rendered);
            }
            return rendered;
        });
    }
}
