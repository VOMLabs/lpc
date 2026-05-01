package com.vomlabs.lpcmmx.commands;

import com.vomlabs.lpcmmx.Main;
import com.vomlabs.lpcmmx.mute.MuteManager;
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

public class MuteCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;
    private final MuteManager muteManager;

    public MuteCommand(Main plugin) {
        this.plugin = plugin;
        this.muteManager = plugin.getMuteManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Usage: /lpc mute <player> [player2...]"));
            return true;
        }

        if (!(sender instanceof Player) && args.length < 2) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Console must specify both source and target players."));
            return true;
        }

        Player source = null;
        if (sender instanceof Player) {
            source = (Player) sender;
        }

        if (args.length == 1 && source != null) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            if (target == null || !target.hasPlayedBefore()) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Player not found: " + args[0]));
                return true;
            }

            if (muteManager.isMuted(source, target.getUniqueId())) {
                muteManager.unmutePlayer(source, target.getUniqueId());
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Unmuted " + target.getName() + "!"));
            } else {
                muteManager.mutePlayer(source, target.getUniqueId());
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Muted " + target.getName() + "!"));
            }
            return true;
        }

        for (String arg : args) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(arg);
            if (target == null || !target.hasPlayedBefore()) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Player not found: " + arg));
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

        sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Updated mute list!"));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(player.getName());
                }
            }
        }
        return completions;
    }
}
