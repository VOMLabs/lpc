package com.vomlabs.lpcmmx;

import com.vomlabs.lpcmmx.api.LPCAPI;
import com.vomlabs.lpcmmx.filter.ChatFilter;
import com.vomlabs.lpcmmx.integration.VaultHook;
import com.vomlabs.lpcmmx.listener.AsyncChatListener;
import com.vomlabs.lpcmmx.messages.MessageManager;
import com.vomlabs.lpcmmx.mute.MuteManager;
import com.vomlabs.lpcmmx.security.EncryptionManager;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    private boolean isPaper;
    private ChatFilter chatFilter;
    private MuteManager muteManager;
    private MessageManager messageManager;
    private EncryptionManager encryptionManager;

    public static LegacyComponentSerializer getLegacySerializer() {
        return LegacyComponentSerializer.builder()
                .character('§')
                .hexColors()
                .useUnusualXRepeatedCharacterHexFormat()
                .build();
    }

    @Override
    public void onEnable() {
        this.isPaper = checkIfPaper();
        this.chatFilter = new ChatFilter(this);
        this.muteManager = new MuteManager(this);
        this.messageManager = new MessageManager(this);
        this.encryptionManager = new EncryptionManager();
        LPCAPI.init(this);
        VaultHook.setupEconomy(this);
        registerCommand();
        saveDefaultConfig();
        messageManager.saveDefaultMessages();
        registerListeners();
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

    private void registerCommand() {
        String commandName = "lpc";
        com.vomlabs.lpcmmx.commands.LPCCommand lpcCommand = new com.vomlabs.lpcmmx.commands.LPCCommand(this);
        com.vomlabs.lpcmmx.commands.MuteCommand muteCommand = new com.vomlabs.lpcmmx.commands.MuteCommand(this);

        this.getCommand(commandName).setExecutor(lpcCommand);
        this.getCommand(commandName).setTabCompleter(lpcCommand);
        this.getCommand("lpc").setExecutor(muteCommand);
    }

    private void registerListeners() {
        if (isPaper) {
            getServer().getPluginManager().registerEvents(new com.vomlabs.lpcmmx.listener.AsyncChatListener(this), this);
        } else {
            getServer().getPluginManager().registerEvents(new com.vomlabs.lpcmmx.listener.SpigotChatListener(this), this);
        }
    }

    public ChatFilter getChatFilter() {
        return chatFilter;
    }

    public MuteManager getMuteManager() {
        return muteManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public EncryptionManager getEncryptionManager() {
        return encryptionManager;
    }

    public VaultHook getVaultHook() {
        return VaultHook.getInstance();
    }
}
