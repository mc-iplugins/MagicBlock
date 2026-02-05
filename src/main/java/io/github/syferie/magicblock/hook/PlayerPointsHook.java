package io.github.syferie.magicblock.hook;

import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerPointsHook {

    private static final PlayerPointsAPI pointsAPI = ((PlayerPoints) Bukkit.getPluginManager().getPlugin("PlayerPoints")).getAPI();

    public static int look(Player player) {
        return pointsAPI.look(player.getUniqueId());
    }

    public static boolean check(int amount, Player player) {
        return pointsAPI.look(player.getUniqueId()) >= amount;
    }

    public static boolean take(int amount, Player player) {
        return pointsAPI.take(player.getUniqueId(), amount);
    }

}
