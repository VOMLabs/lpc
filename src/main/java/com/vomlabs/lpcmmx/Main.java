package com.vomlabs.lpcmmx;

import com.vomlabs.lpcmmx.commands.LPCCommand;
import com.vomlabs.lpcmmx.filter.ChatFilter;
import com.vomlabs.lpcmmx.integration.VaultHook;
import com.vomlabs.lpcmmx.listener.AsyncChatListener;
import com.vomlabs.lpcmmx.mute.MuteManager;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import com.vomlabs.lpcmmx.listener.SpigotChatListener;
import org.bukkit.plugin.java.JavaPlugin;


public final class Main extends JavaPlugin {
    private boolean isPaper;
    private ChatFilter chatFilter;
    private MuteManager muteManager;

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
        this.muteManager = new MuteManager(this);
        VaultHook.setupEconomy(this);
        registerCommand();
        saveDefaultConfig();
        registerListeners();
    }

    public ChatFilter getChatFilter() {
        return chatFilter;
    }

    public MuteManager getMuteManager() {
        return muteManager;
    }

    public void registerCommand() {
        String commandName = "lpc";
        LPCCommand lpcCommand = new LPCCommand(this);
        MuteCommand muteCommand = new MuteCommand(this);

        this.getCommand(commandName).setExecutor(lpcCommand);
        this.getCommand(commandName).setTabCompleter(lpcCommand);
        this.getCommand("lpc").setExecutor(muteCommand);
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
