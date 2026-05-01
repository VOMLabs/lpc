package com.vomlabs.lpcmmx.commands;

import com.vomlabs.lpcmmx.Main;
import com.vomlabs.lpcmmx.messages.MessageManager;
import com.vomlabs.lpcmmx.mute.MuteManager;
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

public class MSGCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;
    private final MessageManager messageManager;
    private final MuteManager muteManager;
    private final HashMap<UUID, Boolean> msgToggle;

    public MSGCommand(Main plugin) {
        this.plugin = plugin;
        this.messageManager = plugin.getMessageManager();
        this.muteManager = plugin.getMuteManager();
        this.msgToggle = new HashMap<>();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(messageManager.getComponent("error.player-only"));
            return true;
        }

        Player source = (Player) sender;

        if (args.length >= 2 && args[0].equalsIgnoreCase("toggle")) {
            if (!source.hasPermission("lpc.msg.toggle")) {
                source.sendMessage(messageManager.getComponent("error.no-permission"));
                return true;
            }
            boolean current = msgToggle.getOrDefault(source.getUniqueId(), true);
            msgToggle.put(source.getUniqueId(), !current);
            if (!current) {
                source.sendMessage(messageManager.getComponent("msg.toggle-on"));
            } else {
                source.sendMessage(messageManager.getComponent("msg.toggle-off"));
            }
            return true;
        }

        if (args.length < 2) {
            HashMap<String, String> placeholders = new HashMap<>();
            placeholders.put("usage", "/msg <player> <message> or /msg toggle");
            source.sendMessage(messageManager.getComponent("msg.usage", placeholders));
            return true;
        }

        if (!source.hasPermission("lpc.msg")) {
            source.sendMessage(messageManager.getComponent("error.no-permission"));
            return true;
        }

        OfflinePlayer targetOff = Bukkit.getOfflinePlayer(args[0]);
        if (targetOff == null || !targetOff.hasPlayedBefore()) {
            HashMap<String, String> placeholders = new HashMap<>();
            placeholders.put("player", args[0]);
            source.sendMessage(messageManager.getComponent("msg.player-not-found", placeholders));
            return true;
        }

        Player target = targetOff.getPlayer();
        if (target == null) {
            HashMap<String, String> placeholders = new HashMap<>();
            placeholders.put("player", args[0]);
            source.sendMessage(messageManager.getComponent("msg.player-not-found", placeholders));
            return true;
        }

        // Check if target has msg disabled
        if (!msgToggle.getOrDefault(target.getUniqueId(), true)) {
            HashMap<String, String> placeholders = new HashMap<>();
            placeholders.put("player", target.getName());
            source.sendMessage(messageManager.getComponent("msg.disabled", placeholders));
            return true;
        }

        // Check if target has source ignored
        if (muteManager.isIgnored(target, source)) {
            HashMap<String, String> placeholders = new HashMap<>();
            placeholders.put("player", target.getName());
            source.sendMessage(messageManager.getComponent("msg.ignored", placeholders));
            return true;
        }

        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            messageBuilder.append(args[i]).append(" ");
        }
        String message = messageBuilder.toString().trim();

        // Send to source
        HashMap<String, String> sendPlaceholders = new HashMap<>();
        sendPlaceholders.put("player", target.getName());
        sendPlaceholders.put("message", message);
        source.sendMessage(messageManager.getComponent("msg.send", sendPlaceholders));

        // Send to target
        HashMap<String, String> receivePlaceholders = new HashMap<>();
        receivePlaceholders.put("player", source.getName());
        receivePlaceholders.put("message", message);
        target.sendMessage(messageManager.getComponent("msg.receive", receivePlaceholders));

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
