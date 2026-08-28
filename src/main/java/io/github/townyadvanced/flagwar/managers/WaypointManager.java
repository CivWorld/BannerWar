package io.github.townyadvanced.flagwar.managers;

import io.github.townyadvanced.flagwar.objects.CellUnderAttack;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import town.sheepy.wayfinderAPI.WayfinderAPI;
import town.sheepy.wayfinderAPI.WaypointService;
import town.sheepy.wayfinderAPI.model.WaypointStyle;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

public final class WaypointManager {

    /** Color used by Wayfinder while an infernal flag displays ancient debris. */
    private static final Color ANCIENT_DEBRIS_COLOR = Color.fromRGB(0x4A2A23);

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

        try {

        SERVICE.createWaypoint(
            toKey(c.getNameOfFlagOwner()),
            c.getFlagBaseBlock().getLocation(),
            getMaterialColor(c.getFlagTimerBlockLocation().getBlock().getType()).orElse(Color.RED),
            WaypointStyle.FLAG,
            1000); // hardcoded for now.
        } catch (IllegalArgumentException e) {
            LOGGER.warning(e.getMessage() + " The waypoint was not created.");
        }
    }

    /**
     * Updates a waypoint to match its flag's current wool, or the infernal ancient-debris color.
     * Temporary non-colored materials, such as the invincibility block, leave the current color unchanged.
     *
     * @param cell the flag whose waypoint color should be synchronized
     */
    public void updateWaypointColor(CellUnderAttack cell) {
        if (SERVICE == null) return;

        String id = toKey(cell.getNameOfFlagOwner());
        if (!SERVICE.exists(id)) return;

        var waypoint = SERVICE.get(id);
        if (waypoint == null || !isSameBlock(waypoint.location(), cell.getFlagBaseBlock().getLocation())) return;

        getMaterialColor(cell.getFlagTimerBlockLocation().getBlock().getType())
            .ifPresent(color -> SERVICE.setWaypointColor(id, color));
    }

    private Optional<Color> getMaterialColor(Material material) {
        if (material == Material.ANCIENT_DEBRIS) return Optional.of(ANCIENT_DEBRIS_COLOR);

        String materialName = material.name();
        String woolSuffix = "_WOOL";
        if (!materialName.endsWith(woolSuffix)) return Optional.empty();

        String dyeName = materialName.substring(0, materialName.length() - woolSuffix.length());
        return Optional.of(DyeColor.valueOf(dyeName).getColor());
    }

    private boolean isSameBlock(Location first, Location second) {
        return Objects.equals(first.getWorld(), second.getWorld())
            && first.getBlockX() == second.getBlockX()
            && first.getBlockY() == second.getBlockY()
            && first.getBlockZ() == second.getBlockZ();
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
