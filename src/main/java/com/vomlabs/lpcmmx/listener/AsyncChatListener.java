package com.vomlabs.lpcmmx.listener;

import com.vomlabs.lpcmmx.Main;
import com.vomlabs.lpcmmx.filter.ChatFilter;
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

        if (filter.isAntiSpamEnabled() && filter.isSpamming(player.getUniqueId())) {
            player.sendMessage(Component.text("§cPlease slow down!"));
            event.setCancelled(true);
            return;
        }

        if (filter.isSwearFilterEnabled()) {
            String plainMessage = PlainTextComponentSerializer.plainText().serialize(event.message());
            String filtered = filter.filterSwearWords(plainMessage);
            if (!plainMessage.equals(filtered)) {
                event.message(Component.text(filtered));
            }
        }

        if(!plugin.getConfig().getBoolean("use-item-placeholder", false) || !player.hasPermission("lpc.itemplaceholder")){
            event.renderer(lpcChatRenderer);
            return;
        }

        final ItemStack item = player.getInventory().getItemInMainHand();
        Component displayName = item.getItemMeta() != null && item.getItemMeta().hasDisplayName() ? item.getItemMeta().displayName() : Component.text(item.getType().toString().toLowerCase().replace("_", " "));
        if (item.getType().equals(Material.AIR) || displayName == null) {
            event.renderer(lpcChatRenderer);
            return;
        }

        if (filter.isSwearFilterEnabled() && filter.filterItemNames) {
            String filteredName = filter.filterItemName(PlainTextComponentSerializer.plainText().serialize(displayName));
            displayName = Component.text(filteredName);
        }

        final Component finalDisplayName = displayName;

        event.renderer((source, sourceDisplayName, message, viewer) -> lpcChatRenderer.render(source, sourceDisplayName, message, viewer)
                .replaceText(TextReplacementConfig.builder().match(compile("\\[item]", CASE_INSENSITIVE))
                        .replacement(finalDisplayName.hoverEvent(item)).build()));
    }
}
