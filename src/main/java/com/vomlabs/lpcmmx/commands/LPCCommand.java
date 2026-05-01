package com.vomlabs.lpcmmx.commands;

import com.vomlabs.lpcmmx.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import com.vomlabs.lpcmmx.Main;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LPCCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;

    public LPCCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 1 && "reload".equals(args[0])) {
            plugin.reloadConfig();
            String rawReloadMessage = plugin.getConfig().getString("reload-message", "<green>Reloaded LPC Configuration!</green>");
            Component message = MiniMessage.miniMessage().deserialize(rawReloadMessage);

            if (plugin.getServer().getName().toLowerCase().contains("paper")) {
                sender.sendMessage(message);
            } else {
                sender.sendMessage(Main.getLegacySerializer().serialize(message));
            }
            return true;
        }
        return false;
    }


    public List<String> onTabComplete(final @NotNull CommandSender sender, final @NotNull Command command, final @NotNull String alias, final String[] args) {
        if (args.length == 1)
            return Collections.singletonList("reload");

        return new ArrayList<>();
    }
}
