package com.vomlabs.lpcmmx.commands;

import com.vomlabs.lpcmmx.Main;
import com.vomlabs.lpcmmx.messages.MessageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
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
        sender.sendMessage(messageManager.getComponent("error.usage.lpc"));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            if (sender.hasPermission("lpc.reload")) completions.add("reload");
        }
        return completions;
    }
}
