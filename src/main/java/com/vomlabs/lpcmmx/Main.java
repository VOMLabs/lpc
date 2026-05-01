package com.vomlabs.lpcmmx;

import com.vomlabs.lpcmmx.api.LPCAPI;
import com.vomlabs.lpcmmx.commands.LPCCommand;
import com.vomlabs.lpcmmx.commands.MSGCommand;
import com.vomlabs.lpcmmx.commands.MuteCommand;
import com.vomlabs.lpcmmx.discord.DiscordWebhook;
import com.vomlabs.lpcmmx.filter.ChatFilter;
import com.vomlabs.lpcmmx.integration.VaultHook;
import com.vomlabs.lpcmmx.listener.AsyncChatListener;
import com.vomlabs.lpcmmx.logging.ChatLogger;
import com.vomlabs.lpcmmx.messages.MessageManager;
import com.vomlabs.lpcmmx.mute.MuteManager;
import com.vomlabs.lpcmmx.security.EncryptionManager;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import dev.faststats.bukkit.BukkitMetrics;
import dev.faststats.core.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    private boolean isPaper;
    private ChatFilter chatFilter;
    private MuteManager muteManager;
    private MessageManager messageManager;
    private EncryptionManager encryptionManager;
    private DiscordWebhook discordWebhook;
    private ChatLogger chatLogger;
    private final Metrics metrics = BukkitMetrics.factory()
            .token("a160a80b2714f4d220f6c4de213a8928")
            .create(this);

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
        this.discordWebhook = new DiscordWebhook(this);
        this.chatLogger = new ChatLogger(this);
        LPCAPI.init(this);
        VaultHook.setupEconomy(this);
        registerCommand();
        saveDefaultConfig();
        messageManager.saveDefaultMessages();
        registerListeners();
        metrics.ready();
        getLogger().info("FastStats metrics initialized");
    }

    @Override
    public void onDisable() {
        chatLogger.close();
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
        LPCCommand lpcCommand = new LPCCommand(this);
        MuteCommand muteCommand = new MuteCommand(this);
        MSGCommand msgCommand = new MSGCommand(this);

        this.getCommand(commandName).setExecutor(lpcCommand);
        this.getCommand(commandName).setTabCompleter(lpcCommand);
        this.getCommand("lpc").setExecutor(muteCommand);
        this.getCommand("msg").setExecutor(msgCommand);
        this.getCommand("msg").setTabCompleter(msgCommand);
    }

    private void registerListeners() {
        if (isPaper) {
            getServer().getPluginManager().registerEvents(new AsyncChatListener(this), this);
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

    public DiscordWebhook getDiscordWebhook() {
        return discordWebhook;
    }

    public ChatLogger getChatLogger() {
        return chatLogger;
    }

    public VaultHook getVaultHook() {
        return VaultHook.getInstance();
    }

    public void reloadMessageManager() {
        messageManager.reloadAsync().thenRun(() ->
            getLogger().info("Messages reloaded asynchronously")
        );
    }
}
