package io.github.syferie.magicblock.hook;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook {
    private static Economy economy;

    static {
        RegisteredServiceProvider<Economy> provider = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (provider != null) {
            economy = (Economy)provider.getProvider();
        }
    }

    public static boolean give(double count, OfflinePlayer player) {
        EconomyResponse response = economy.depositPlayer(player, count);
        return response.transactionSuccess();
    }

    public static boolean take(double count, Player player) {
        EconomyResponse response = economy.withdrawPlayer((OfflinePlayer)player, count);
        return response.transactionSuccess();
    }

    public static boolean check(double count, Player player) {
        return economy.has(player, count);
    }

    public static double look(Player player) {
        return economy.getBalance(player);
    }

}