package com.vomlabs.lpcmmx;

import com.vomlabs.lpcmmx.commands.LPCCommand;
import com.vomlabs.lpcmmx.filter.ChatFilter;
import com.vomlabs.lpcmmx.integration.VaultHook;
import com.vomlabs.lpcmmx.listener.AsyncChatListener;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import com.vomlabs.lpcmmx.listener.SpigotChatListener;
import org.bukkit.plugin.java.JavaPlugin;


public final class Main extends JavaPlugin {
    private boolean isPaper;
    private ChatFilter chatFilter;

    private static final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.builder()
            .character('§')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    public static LegacyComponentSerializer getLegacySerializer() {
        return legacySerializer;
    }

    @Override
    public void onEnable() {
        this.isPaper = checkIfPaper();
        this.chatFilter = new ChatFilter(this);
        VaultHook.setupEconomy(this);
        registerCommand();
        saveDefaultConfig();
        registerListeners();
    }

    public ChatFilter getChatFilter() {
        return chatFilter;
    }

    public void registerCommand() {
        String commandName = "lpc";
        LPCCommand lpcCommand = new LPCCommand(this);

        this.getCommand(commandName).setExecutor(lpcCommand);
        this.getCommand(commandName).setTabCompleter(lpcCommand);
    }

    private boolean checkIfPaper() {
        try {
            Class.forName("io.papermc.paper.event.player.AsyncChatEvent");
            getLogger().info("Paper API has been detected and will be used.");
            return true;
        } catch (ClassNotFoundException e) {
            getLogger().info("Spigot API has been detected and will be used.");
            return false;
        }
    }

    private void registerListeners() {
        if (isPaper) {
            getServer().getPluginManager().registerEvents(new AsyncChatListener(this), this);
        } else {
            getServer().getPluginManager().registerEvents(new SpigotChatListener(this), this);
        }
    }

}
