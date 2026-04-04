package io.github.townyadvanced.flagwar.util;

import io.github.townyadvanced.flagwar.FlagWar;
import io.github.townyadvanced.flagwar.config.BannerWarConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/** A class that contains various functions that prepares messages to be broadcast to one or all players. */
public final class Broadcasts {

    /** Holds a {@link Map} of every player's most recent message sent to prevent spamming. */
    private static final Map<UUID, String> LAST_MESSAGES = new HashMap<>();

    private Broadcasts() {}

    /**
     * Sends a message to the specified {@link Player}, formatted for BannerWar.
     * @param p the specified {@link Player}
     * @param msg the message
     */
    public static void sendMessage(Player p, String msg) {
        sendMessage(p, msg, ChatColor.RESET);
    }

    /**
     * Sends a message to the specified {@link Player}, formatted for BannerWar.
     * @param p the specified {@link Player}
     * @param msg the message
     * @param color the {@link ChatColor} that this message will be in
     */
    public static void sendMessage(Player p, String msg, ChatColor color) {
        String formatted = prepareMessage(color + msg);
        UUID id = p.getUniqueId();

        if (Objects.equals(formatted, LAST_MESSAGES.get(id))) return;
        p.sendMessage(formatted);
        LAST_MESSAGES.put(id, formatted);
        resetLastMessage(id);
    }

    /**
     * Sends a message to the specified {@link Player}, formatted for BannerWar.
     * This message bypasses the repeated message filter that {@link #sendMessage(Player, String)} has.
     * @param p the specified {@link Player}
     * @param msg the message
     */
    public static void sendMessageNoFilter(Player p, String msg) {
        sendMessageNoFilter(p, msg, ChatColor.RESET);
    }

    /**
     * Sends a message to the specified {@link Player}, formatted for BannerWar.
     * This message bypasses the repeated message filter that {@link #sendMessage(Player, String, ChatColor)} has.
     * @param p the specified {@link Player}
     * @param msg the message
     * @param color the {@link ChatColor} that this message will be in
     */
    public static void sendMessageNoFilter(Player p, String msg, ChatColor color) {
        String formatted = prepareMessage(color + msg);
        UUID id =  p.getUniqueId();

        p.sendMessage(formatted);
        LAST_MESSAGES.put(id, formatted);
        resetLastMessage(id);
    }

    /**
     * Sends an error message to the specified {@link Player}, formatted for BannerWar.
     * <p>
     * This message's purpose is to let a player know that an action has failed or been blocked by the server.
     * @param p the specified {@link Player}
     * @param msg the message
     */
    public static void sendErrorMessage(Player p, String msg) {
        String formatted = prepareErrorMessage(msg);
        UUID id = p.getUniqueId();

        if (Objects.equals(formatted, LAST_MESSAGES.get(id))) return;

        p.sendMessage(formatted);
        LAST_MESSAGES.put(id, formatted);
        resetLastMessage(id);
    }

    /**
     * Sends an error message to the specified {@link Player}, formatted for BannerWar.
     * This message bypasses the repeated message filter that {@link #sendErrorMessage(Player, String)} has.
     * <p>
     * This message's purpose is to let a player know that an action has failed or been blocked by the server.
     * @param p the specified {@link Player}
     * @param msg the message
     */
    public static void sendErrorMessageNoFilter(Player p, String msg) {
        String formatted = prepareErrorMessage(msg);
        UUID id = p.getUniqueId();

        p.sendMessage(formatted);
        LAST_MESSAGES.put(id, formatted);
        resetLastMessage(id);
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

        StringBuilder out = new StringBuilder();

        var parts = msg.split("\n");

        out.append(buildPrefix(false)).append(parts[0]).append("\n");

        if (parts.length < 2) return out.toString();

        else {
            for (int i = 1; i < parts.length; i++) {
                out.append(BannerWarConfig.getBracketColor()).append("[] ").append(ChatColor.RESET).append(parts[i]).append("\n");
            }
        }

        return out.toString();
    }

    /**
     * Returns a message, formatted for BannerWar error reports.
     * @param msg the message
     */
    public static String prepareErrorMessage(String msg) {
        return buildPrefix(false) + ChatColor.RED + msg;
    }

    /**
     * Returns a message, formatted for BannerWar broadcasts.
     * @param msg the message
     */
    public static String prepareBroadcastMessage(String msg) {
        return buildPrefix(true) + ChatColor.RESET + msg;
    }

    /**
     * Builds and returns the prefix of the entity providing broadcasts.
     * @param isServerBroadcast whether the prefix should be built to accommodate a server broadcast
     */
    private static String buildPrefix(boolean isServerBroadcast) {
        final ChatColor NAME_COLOR = BannerWarConfig.getNameColor();
        final ChatColor BRACKET_COLOR = BannerWarConfig.getBracketColor();
        final String SERVER_PART = isServerBroadcast ? ChatColor.BOLD + "" : "";
        final String NAME = BannerWarConfig.getBroadcasterName();

        return BRACKET_COLOR + SERVER_PART + "["
            + NAME_COLOR + SERVER_PART + NAME
            + BRACKET_COLOR + SERVER_PART + "] "
            + ChatColor.RESET;
    }

    /**
     * Builds the prefix of the entity providing broadcasts, without any color usage.
     */
    private static String buildPlainPrefix() {
        return "[" + BannerWarConfig.getBroadcasterName() + "]";
    }

    /**
     * Resets the {@link #LAST_MESSAGES} value of the specified key in an amount of time.
     * @param id the specified key
     */
    private static void resetLastMessage(UUID id) {
        Bukkit.getScheduler().runTaskLater(FlagWar.getInstance(), () -> LAST_MESSAGES.remove(id), 100);
    }
}
