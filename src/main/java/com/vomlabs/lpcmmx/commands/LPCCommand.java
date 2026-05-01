package com.vomlabs.lpcmmx.commands;

import com.vomlabs.lpcmmx.Main;
import com.vomlabs.lpcmmx.format.PlayerFormatManager;
import com.vomlabs.lpcmmx.messages.MessageManager;
import com.vomlabs.lpcmmx.suspend.ChatSuspendManager;
import com.vomlabs.lpcmmx.report.ReportManager;
import com.vomlabs.lpcmmx.translation.TranslationManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

        // Chat Suspend commands
        if (args.length >= 1 && "suspend".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("lpc.suspend")) {
                sender.sendMessage(messageManager.getComponent("error.no-permission"));
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(messageManager.getComponent("error.usage.suspend"));
                return true;
            }
            String targetName = args[1];
            Player target = Bukkit.getPlayer(targetName);
            if (target == null) {
                sender.sendMessage(messageManager.getComponent("error.player-not-found", "player", targetName));
                return true;
            }
            long duration = 300000; // 5 minutes default
            String reason = "No reason";
            if (args.length >= 3) {
                try {
                    duration = Long.parseLong(args[2]) * 1000L;
                } catch (NumberFormatException e) {
                    reason = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
                }
            }
            if (args.length >= 4) {
                reason = String.join(" ", java.util.Arrays.copyOfRange(args, 3, args.length));
            }
            ChatSuspendManager suspendManager = plugin.getSuspendManager();
            suspendManager.suspendPlayer(sender instanceof Player ? (Player) sender : null,
                    target.getUniqueId(), duration, reason);
            sender.sendMessage(messageManager.getComponent("suspend.suspended",
                    "player", target.getName(), "reason", reason));
            target.sendMessage(messageManager.getComponent("suspend.notify",
                    "reason", reason));
            return true;
        }

        if (args.length >= 1 && "unsuspend".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("lpc.suspend")) {
                sender.sendMessage(messageManager.getComponent("error.no-permission"));
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(messageManager.getComponent("error.usage.unsuspend"));
                return true;
            }
            String targetName = args[1];
            Player target = Bukkit.getPlayer(targetName);
            if (target == null) {
                sender.sendMessage(messageManager.getComponent("error.player-not-found", "player", targetName));
                return true;
            }
            ChatSuspendManager suspendManager = plugin.getSuspendManager();
            suspendManager.unsuspendPlayer(target.getUniqueId());
            sender.sendMessage(messageManager.getComponent("suspend.unsuspended", "player", target.getName()));
            return true;
        }

        // Translation toggle
        if (args.length >= 1 && "translate".equalsIgnoreCase(args[0])) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(messageManager.getComponent("error.player-only"));
                return true;
            }
            Player player = (Player) sender;
            if (!player.hasPermission("lpc.translate")) {
                sender.sendMessage(messageManager.getComponent("error.no-permission"));
                return true;
            }
            TranslationManager translationManager = plugin.getTranslationManager();
            translationManager.toggleTranslation(player.getUniqueId());
            if (translationManager.isTranslationEnabled(player.getUniqueId())) {
                sender.sendMessage(messageManager.getComponent("translate.enabled"));
            } else {
                sender.sendMessage(messageManager.getComponent("translate.disabled"));
            }
            return true;
        }

        // Report player
        if (args.length >= 1 && "report".equalsIgnoreCase(args[0])) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(messageManager.getComponent("error.player-only"));
                return true;
            }
            Player reporter = (Player) sender;
            if (!reporter.hasPermission("lpc.report")) {
                sender.sendMessage(messageManager.getComponent("error.no-permission"));
                return true;
            }
            if (args.length < 3) {
                sender.sendMessage(messageManager.getComponent("error.usage.report"));
                return true;
            }
            String targetName = args[1];
            Player target = Bukkit.getPlayer(targetName);
            if (target == null) {
                sender.sendMessage(messageManager.getComponent("error.player-not-found", "player", targetName));
                return true;
            }
            String reason = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
            ReportManager reportManager = plugin.getReportManager();
            reportManager.reportPlayer(reporter, target.getUniqueId(), reason);
            return true;
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
                if (plugin.getConfig().getConfigurationSection("player-formats") != null) {
                    for (String key : plugin.getConfig().getConfigurationSection("player-formats").getKeys(false)) {
                        completions.add(key);
                    }
                }
            }
        }
        if (args.length == 1) {
            if (sender.hasPermission("lpc.suspend")) {
                completions.add("suspend");
                completions.add("unsuspend");
            }
            if (sender instanceof Player && sender.hasPermission("lpc.translate")) {
                completions.add("translate");
            }
            if (sender.hasPermission("lpc.report")) {
                completions.add("report");
            }
        }
        return completions;
    }
}
