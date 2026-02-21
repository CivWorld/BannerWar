package io.github.townyadvanced.flagwar;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public final class Broadcasts {

    /** Holds the BannerWar prefix used for in-server broadcasts. */
    private static final String PREFIX =
        ChatColor.DARK_PURPLE + "[" + ChatColor.YELLOW + "BannerWar" + ChatColor.DARK_PURPLE + "] " + ChatColor.RESET;

    private Broadcasts() {}

    /**
     * Sends a message to the specified {@link Player}, formatted for BannerWar.
     * @param p the specified {@link Player}
     * @param msg the message
     */
    public static void sendMessage(Player p, String msg) {
        p.sendMessage(PREFIX + msg);
    }

    /**
     * Broadcasts a message to the entire server, formatted for BannerWar.
     * @param msg the message
     */
    public static void broadcastMessage(String msg) {
        Bukkit.getServer().broadcastMessage(PREFIX + msg);
    }

}
