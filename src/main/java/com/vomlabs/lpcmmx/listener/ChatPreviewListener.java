package com.vomlabs.lpcmmx.listener;

import com.vomlabs.lpcmmx.Main;
import com.vomlabs.lpcmmx.renderer.LPCChatRenderer;
import io.papermc.paper.event.packet.PlayerChatPreviewEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventListener;
import org.jetbrains.annotations.NotNull;

public class ChatPreviewListener implements EventListener {

    private final Main plugin;
    private final LPCChatRenderer renderer;

    public ChatPreviewListener(Main plugin) {
        this.plugin = plugin;
        this.renderer = new LPCChatRenderer(plugin);
    }

    @EventHandler
    public void onChatPreview(@NotNull PlayerChatPreviewEvent event) {
        if (!plugin.getConfig().getBoolean("chat-preview.enabled", true)) {
            return;
        }

        Player player = event.getPlayer();
        Component previewComponent = renderer.render(
                player,
                player.displayName(),
                event.originalMessage(),
                player
        );

        event.preview(previewComponent);
    }
}
