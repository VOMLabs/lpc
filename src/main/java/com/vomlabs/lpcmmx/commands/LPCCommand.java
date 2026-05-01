package com.vomlabs.lpcmmx.commands;

import com.vomlabs.lpcmmx.Main;
import com.vomlabs.lpcmmx.format.PlayerFormatManager;
import com.vomlabs.lpcmmx.messages.MessageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class LPCCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;
    private final MessageManager messageManager;

    public LPCCommand(Main plugin) {
        this.plugin = plugin;
        this.messageManager = plugin.getMessageManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 1 && "reload".equals(args[0])) {
            if (!sender.hasPermission("lpc.reload")) {
                sender.sendMessage(messageManager.getComponent("error.no-permission"));
                return true;
            }
            plugin.reloadConfig();
            messageManager.reloadAsync();
            if (plugin.getServer().getName().toLowerCase().contains("paper")) {
                sender.sendMessage(messageManager.getComponent("reload"));
            } else {
                sender.sendMessage(Main.getLegacySerializer().serialize(messageManager.getComponent("reload")));
            }
            return true;
        }

        if (args.length >= 1 && "format".equalsIgnoreCase(args[0])) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(messageManager.getComponent("error.player-only"));
                return true;
            }
            Player player = (Player) sender;
            PlayerFormatManager formatManager = plugin.getPlayerFormatManager();

            if (args.length == 1) {
                // Show current format
                if (formatManager.hasCustomFormat(player)) {
                    String formatName = formatManager.getPlayerFormat(player);
                    sender.sendMessage(messageManager.getComponent("format.current", "format", formatName));
                } else {
                    sender.sendMessage(messageManager.getComponent("format.none"));
                }
                return true;
            }

            if (args.length == 2) {
                if (!sender.hasPermission("lpc.format")) {
                    sender.sendMessage(messageManager.getComponent("error.no-permission"));
                    return true;
                }

                String formatName = args[1];
                if ("clear".equalsIgnoreCase(formatName) || "none".equalsIgnoreCase(formatName)) {
                    formatManager.clearPlayerFormat(player);
                    sender.sendMessage(messageManager.getComponent("format.cleared"));
                    return true;
                }

                if (!formatManager.formatExists(formatName)) {
                    sender.sendMessage(messageManager.getComponent("format.not-found", "format", formatName));
                    return true;
                }

                formatManager.setPlayerFormat(player, formatName);
                sender.sendMessage(messageManager.getComponent("format.set", "format", formatName));
                return true;
            }
        }

        sender.sendMessage(messageManager.getComponent("error.usage.lpc"));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            if (sender.hasPermission("lpc.reload")) completions.add("reload");
            if (sender.hasPermission("lpc.format")) completions.add("format");
        }
        if (args.length == 2 && "format".equalsIgnoreCase(args[0])) {
            if (sender.hasPermission("lpc.format")) {
                completions.add("clear");
                // Add configured format names
                for (String key : plugin.getConfig().getConfigurationSection("player-formats").getKeys(false)) {
                    completions.add(key);
                }
            }
        }
        return completions;
    }
}
