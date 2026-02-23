package io.github.townyadvanced.flagwar.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
/** A class that contains various functions that prepares messages to be broadcast to one or all players. */
public final class Broadcasts {

    /** Holds the BannerWar prefix used for in-server broadcasts. */
    private static final String PREFIX =
        ChatColor.DARK_PURPLE + "[" + ChatColor.YELLOW + "BannerWar" + ChatColor.DARK_PURPLE + "] " + ChatColor.RESET;

    /** Holds the BannerWar prefix used, without any color usage. */
    private static final String PLAIN_PREFIX =
         "[BannerWar]";

    private Broadcasts() {}

    /**
     * Sends a message to the specified {@link Player}, formatted for BannerWar.
     * @param p the specified {@link Player}
     * @param msg the message
     */
    public static void sendMessage(Player p, String msg) {
        p.sendMessage(prepareMessage(msg));
    }

    /**
     * Broadcasts a message to the entire server, formatted for BannerWar.
     * @param msg the message
     */
    public static void broadcastMessage(String msg) {
        Bukkit.getServer().broadcastMessage(prepareBroadcastMessage(msg));
    }

    /**
     * Returns a message, formatted for BannerWar generic messages.
     * @param msg the message
     */
    public static String prepareMessage(String msg) {
        return PREFIX + msg;
    }

    /**
     * Returns a message, formatted for BannerWar error reports.
     * @param msg the message
     */
    public static String prepareErrorMessage(String msg) {
        return PREFIX + ChatColor.RED + msg;
    }

    /**
     * Returns a message, formatted for BannerWar broadcasts.
     * @param msg the message
     */
    public static String prepareBroadcastMessage(String msg) {
        return ChatColor.ITALIC + PREFIX + ChatColor.RESET + msg;
    }

    /**
     * Sends an error message to the specified {@link Player}, formatted for BannerWar.
     * @param p the specified {@link Player}
     * @param msg the message
     */
    public static void sendErrorMessage(Player p, String msg) {
        p.sendMessage(prepareErrorMessage(msg));
    }
}
