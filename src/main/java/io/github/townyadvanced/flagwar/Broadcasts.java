package io.github.townyadvanced.flagwar;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public final class Broadcasts {

    /** Holds the BannerWar prefix used for in-server broadcasts. */
    private static final String PREFIX =
        ChatColor.DARK_PURPLE + "[" + ChatColor.YELLOW + "BannerWar" + ChatColor.DARK_PURPLE + "] " + ChatColor.RESET;

    private Broadcasts() {}

    public static void sendMessage(Player p, String message) {
        p.sendMessage(PREFIX + message);
    }

}
