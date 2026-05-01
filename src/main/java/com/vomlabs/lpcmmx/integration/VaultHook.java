package com.vomlabs.lpcmmx.integration;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.Plugin;

public class VaultHook {

    private static Economy economy = null;

    public static boolean setupEconomy(Plugin plugin) {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        plugin.getLogger().info("Vault economy integration enabled.");
        return true;
    }

    public static boolean hasEconomy() {
        return economy != null;
    }

    public static String format(double amount) {
        if (economy == null) return String.valueOf(amount);
        return economy.format(amount);
    }

    public static double getBalance(org.bukkit.OfflinePlayer player) {
        if (economy == null) return 0.0;
        return economy.getBalance(player);
    }

    public static String getFormattedBalance(org.bukkit.OfflinePlayer player) {
        if (economy == null) return "0";
        return format(economy.getBalance(player));
    }
}
