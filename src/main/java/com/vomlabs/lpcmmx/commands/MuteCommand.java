package com.vomlabs.lpcmmx.commands;

import com.vomlabs.lpcmmx.Main;
import com.vomlabs.lpcmmx.mute.MuteManager;
import com.vomlabs.lpcmmx.messages.MessageManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class MuteCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;
    private final MuteManager muteManager;
    private final MessageManager messageManager;

    public MuteCommand(Main plugin) {
        this.plugin = plugin;
        this.muteManager = plugin.getMuteManager();
        this.messageManager = plugin.getMessageManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(messageManager.getComponent("error.usage.mute"));
            return true;
        }

        if (!(sender instanceof Player) && args.length < 2) {
            sender.sendMessage(messageManager.getComponent("error.console-usage"));
            return true;
        }

        Player source = null;
        if (sender instanceof Player) {
            source = (Player) sender;
        }

        if (args.length == 1 && source != null) {
            if (!sender.hasPermission("lpc.mute.toggle")) {
                sender.sendMessage(messageManager.getComponent("error.no-permission"));
                return true;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            if (target == null || !target.hasPlayedBefore()) {
                HashMap<String, String> placeholders = new HashMap<>();
                placeholders.put("player", args[0]);
                sender.sendMessage(messageManager.getComponent("mute.player-not-found", placeholders));
                return true;
            }

            if (muteManager.isMuted(source, target.getUniqueId())) {
                muteManager.unmutePlayer(source, target.getUniqueId());
                HashMap<String, String> placeholders = new HashMap<>();
                placeholders.put("player", target.getName());
                sender.sendMessage(messageManager.getComponent("mute.unmuted", placeholders));
            } else {
                muteManager.mutePlayer(source, target.getUniqueId());
                HashMap<String, String> placeholders = new HashMap<>();
                placeholders.put("player", target.getName());
                sender.sendMessage(messageManager.getComponent("mute.muted", placeholders));
            }
            return true;
        }

        if (!sender.hasPermission("lpc.mute")) {
            sender.sendMessage(messageManager.getComponent("error.no-permission"));
            return true;
        }

        for (String arg : args) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(arg);
            if (target == null || !target.hasPlayedBefore()) {
                HashMap<String, String> placeholders = new HashMap<>();
                placeholders.put("player", arg);
                sender.sendMessage(messageManager.getComponent("mute.player-not-found", placeholders));
                continue;
            }

            if (source != null) {
                if (muteManager.isMuted(source, target.getUniqueId())) {
                    muteManager.unmutePlayer(source, target.getUniqueId());
                } else {
                    muteManager.mutePlayer(source, target.getUniqueId());
                }
            }
        }

        sender.sendMessage(messageManager.getComponent("mute.mute-list-updated"));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1 && sender.hasPermission("lpc.mute.toggle")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(player.getName());
                }
            }
        }
        return completions;
    }
}
