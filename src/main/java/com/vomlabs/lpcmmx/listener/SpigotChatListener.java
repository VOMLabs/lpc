package com.vomlabs.lpcmmx.listener;

import com.vomlabs.lpcmmx.Main;
import com.vomlabs.lpcmmx.filter.ChatFilter;
import net.kyori.adventure.text.Component;
import com.vomlabs.lpcmmx.renderer.SpigotChatRenderer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;


public class SpigotChatListener implements Listener {
    private final Main plugin;
    private final SpigotChatRenderer chatRenderer;
    private final Map<String, String> legacyToMiniMessageColors;

    public SpigotChatListener(Main plugin) {
        this.plugin = plugin;
        this.chatRenderer = new SpigotChatRenderer(plugin);
        this.legacyToMiniMessageColors = new HashMap<>();
        initColorMappings();
    }

    private void initColorMappings() {
        legacyToMiniMessageColors.put("&0", "<black>");
        legacyToMiniMessageColors.put("&1", "<dark_blue>");
        legacyToMiniMessageColors.put("&2", "<dark_green>");
        legacyToMiniMessageColors.put("&3", "<dark_aqua>");
        legacyToMiniMessageColors.put("&4", "<dark_red>");
        legacyToMiniMessageColors.put("&5", "<dark_purple>");
        legacyToMiniMessageColors.put("&6", "<gold>");
        legacyToMiniMessageColors.put("&7", "<gray>");
        legacyToMiniMessageColors.put("&8", "<dark_gray>");
        legacyToMiniMessageColors.put("&9", "<blue>");
        legacyToMiniMessageColors.put("&a", "<green>");
        legacyToMiniMessageColors.put("&b", "<aqua>");
        legacyToMiniMessageColors.put("&c", "<red>");
        legacyToMiniMessageColors.put("&d", "<light_purple>");
        legacyToMiniMessageColors.put("&e", "<yellow>");
        legacyToMiniMessageColors.put("&f", "<white>");
        legacyToMiniMessageColors.put("&l", "<bold>");
        legacyToMiniMessageColors.put("&o", "<italic>");
        legacyToMiniMessageColors.put("&n", "<underlined>");
        legacyToMiniMessageColors.put("&m", "<strikethrough>");
        legacyToMiniMessageColors.put("&k", "<obfuscated>");
        legacyToMiniMessageColors.put("&r", "<reset>");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        ChatFilter filter = plugin.getChatFilter();

        if (filter.isAntiSpamEnabled() && filter.isSpamming(player.getUniqueId())) {
            player.sendMessage("§cPlease slow down!");
            event.setCancelled(true);
            return;
        }

        String message = event.getMessage();

        if (filter.isSwearFilterEnabled()) {
            message = filter.filterSwearWords(message);
        }

        if (player.hasPermission("lpc.chatcolor")) {
            message = message.replaceAll("§", "&");
            for (Map.Entry<String, String> entry : legacyToMiniMessageColors.entrySet()) {
                message = message.replace(entry.getKey(), entry.getValue());
            }
        } else {
            for (Map.Entry<String, String> entry : legacyToMiniMessageColors.entrySet()) {
                message = message.replace(entry.getValue(), entry.getKey());
            }
        }

        if (plugin.getConfig().getBoolean("use-item-placeholder", false) && player.hasPermission("lpc.itemplaceholder")) {
            final ItemStack item = player.getInventory().getItemInMainHand();
            if (!item.getType().equals(Material.AIR)) {
                String itemName = item.getType().toString().toLowerCase().replace("_", " ");
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    StringBuilder hoverText = new StringBuilder();

                    if (meta.hasDisplayName()) {
                        try {
                            Component displayName = meta.displayName();
                            if (displayName != null) {
                                itemName = MiniMessage.miniMessage().serialize(displayName);
                            }
                        } catch (NoSuchMethodError e) {
                            String displayName = meta.getDisplayName();
                            itemName = MiniMessage.miniMessage().serialize(
                                    LegacyComponentSerializer.builder()
                                            .useUnusualXRepeatedCharacterHexFormat()
                                            .hexColors()
                                            .character('§')
                                            .build()
                                            .deserialize(displayName)
                            );
                        }

                        if (filter.isSwearFilterEnabled() && filter.filterItemNames) {
                            itemName = filter.filterItemName(itemName);
                        }
                    }

                    if (meta.hasLore()) {
                        try {
                            java.util.List<Component> lore = meta.lore();
                            if (lore != null) {
                                for (Component line : lore) {
                                    hoverText.append("\n").append(MiniMessage.miniMessage().serialize(line));
                                }
                            }
                        } catch (NoSuchMethodError e) {
                            java.util.List<String> lore = meta.getLore();
                            if (lore != null) {
                                for (String line : lore) {
                                    hoverText.append("\n").append(MiniMessage.miniMessage().serialize(
                                            LegacyComponentSerializer.builder()
                                                    .useUnusualXRepeatedCharacterHexFormat()
                                                    .hexColors()
                                                    .character('§')
                                                    .build()
                                                    .deserialize(line)
                                    ));
                                }
                            }
                        }
                    }

                    itemName = "<hover:show_text:'" + itemName + hoverText.toString() + "'>" + itemName + "</hover>";
                }
                message = message.replaceFirst("(?i)\\[item]", itemName);
            }
        }

        event.setFormat(LegacyComponentSerializer.builder()
                .useUnusualXRepeatedCharacterHexFormat()
                .hexColors()
                .build()
                .serialize(chatRenderer.render(event.getPlayer(), message)));
    }
}
