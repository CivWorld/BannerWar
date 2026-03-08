package io.github.townyadvanced.flagwar.managers;

import io.github.townyadvanced.flagwar.objects.CellUnderAttack;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import town.sheepy.wayfinderAPI.WayfinderAPI;
import town.sheepy.wayfinderAPI.WaypointService;
import town.sheepy.wayfinderAPI.model.WaypointStyle;

import java.util.Collection;
import java.util.logging.Logger;

public final class WaypointManager {

    /** Holds the {@link JavaPlugin} instance. */
    private final JavaPlugin PLUGIN;

    /** Holds the {@link Logger} of this class. */
    private final Logger LOGGER;

    /** Holds the {@link WaypointService} instance. */
    private WaypointService SERVICE;

    public WaypointManager(JavaPlugin plugin) {

        this.PLUGIN = plugin;
        LOGGER = plugin.getLogger();

        Bukkit.getScheduler().runTaskLater(PLUGIN, this::assignAPI, 1);
    }

    private void assignAPI() {
        this.SERVICE = Bukkit.getServer().getPluginManager().getPlugin("WayfinderAPI") != null ?
            JavaPlugin.getPlugin(WayfinderAPI.class).getWaypointService() :
            null;

    }

    public void createWaypoint(CellUnderAttack c) {
        if (isAPIUnavailable()) return;

        SERVICE.createWaypoint(
            toKey(c.getNameOfFlagOwner()),
            c.getFlagBaseBlock().getLocation(),
            Color.RED,
            WaypointStyle.FLAG,
            1000); // hardcoded for now.
    }

    public void deleteWaypoint(String flagOwner) {
        if (isAPIUnavailable()) return;
        SERVICE.deleteWaypoint(toKey(flagOwner));
    }

    public void addPlayersToWaypoint(Collection<Player> players, String flagOwner) {

        if (isAPIUnavailable()) return;
        String ID = toKey(flagOwner);

        for (Player p : players)
            SERVICE.showWaypointToPlayer(p, ID);

    }

    public void removePlayersFromWaypoint(Collection<Player> players, String flagOwner) {

        if (isAPIUnavailable()) return;
        String ID = toKey(flagOwner);

        for (Player p : players)
            SERVICE.hideWaypointFromPlayer(p, ID);
    }

    private String toKey(String flagOwner) {
        return "BATTLE_" + flagOwner;
    }

    private boolean isAPIUnavailable() {
        if (SERVICE == null) {
            LOGGER.warning("WaypointService is not available; cannot assign waypoints to players!");
            return true;
        }
        return false;
    }
}
