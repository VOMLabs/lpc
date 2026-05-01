package com.vomlabs.lpcmmx.integration;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.Plugin;

public class VaultHook {

    private static Economy economy = null;
    private static VaultHook instance;

    public static boolean setupEconomy(Plugin plugin) {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        instance = new VaultHook();
        plugin.getLogger().info("Vault economy integration enabled.");
        return true;
    }

    public static VaultHook getInstance() {
        return instance;
    }

    public boolean hasEconomy() {
        return economy != null;
    }

    public double getBalance(OfflinePlayer player) {
        if (economy == null) return 0.0;
        return economy.getBalance(player);
    }

    public String getFormattedBalance(OfflinePlayer player) {
        if (economy == null) return "0";
        return economy.format(getBalance(player));
    }

    public static boolean hasEconomy() {
        return economy != null;
    }

    public String format(double amount) {
        if (economy == null) return String.valueOf(amount);
        return economy.format(amount);
    }
}
