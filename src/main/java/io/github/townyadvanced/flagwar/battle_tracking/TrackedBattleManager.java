package io.github.townyadvanced.flagwar.battle_tracking;

import com.palmergames.bukkit.towny.object.Town;
import io.github.townyadvanced.flagwar.FlagWar;
import io.github.townyadvanced.flagwar.battle_tracking.model.enums.BattleStatus;
import io.github.townyadvanced.flagwar.battle_tracking.model.results.BattleSnapshot;
import io.github.townyadvanced.flagwar.database.TrackerDatabase;
import io.github.townyadvanced.flagwar.objects.Battle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/** A class that facilitates the initiating, retrieving, storing and completing of {@link TrackedBattle}s. */
public final class TrackedBattleManager {

    /** Holds a map of all tracked battles and the name of their towns. */
    private final Map<String, TrackedBattle> TRACKED_BATTLES = new HashMap<>();

    /** Holds the {@link BukkitTask} that runs every period of time to update and save tracked battles. */
    private BukkitTask heartbeatTask;

    /** Holds the {@link TrackerDatabase} instance. */
    private final TrackerDatabase DATABASE;

    /** Holds the {@link Plugin} instance. */
    private final Plugin PLUGIN = FlagWar.getInstance();

    /** Returns the tracked battle whose battle region contains this location. This can be null. */
    public TrackedBattle getBattleAt(Location location) {
        return getBattleAt(location.toVector());
    }

    /** Returns the tracked battle whose battle region contains this vector position. This can be null. */
    public TrackedBattle getBattleAt(Vector position) {
        for (var battle : TRACKED_BATTLES.values()) {
            if (battle.isInBattleRegion(position)) return battle;
        }
        return null;
    }

    /** Begins tracking the specified BannerWar {@link Battle}. */
    public void trackBattle(Battle battle) {
        Town town = battle.getContestedTown();
        TRACKED_BATTLES.put(town.getName(), new TrackedBattle(town, battle.getAttacker(), battle.getDefender()));
    }

    public TrackedBattleManager(TrackerDatabase database) {
        this.DATABASE = database;
        // HACK: stand by for 5 seconds to allow for all asynchronous loading to occur.
        // could have the battle tracker be restored upon BattleResumeEvent but can't be bothered to implement.
        Bukkit.getScheduler().runTaskLater(PLUGIN, this::start, 100);
    }

    /**
     * A series of operations to be run upon startup. This includes initiating the {@link #heartbeatTask}
     * and retrieving battles from the database via {@link #populateBattles()}}s.
     */
    private void start() {
        populateBattles();

        heartbeatTask = Bukkit.getScheduler().runTaskTimer(PLUGIN,
            () -> {
                for (var TB : TRACKED_BATTLES.values()) {
                    var result = BattleSnapshot.parse(TB, BattleStatus.ONGOING);
                    DATABASE.insertOrUpdateBattle(result).exceptionally(ex -> {
                        ex.printStackTrace();
                        return null;
                    }).thenRun(() -> {
                        try {
                            DATABASE.insertOrUpdatePlayersSync(result.playerResultMap().values(), result.townName());
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }, 400L, 400L); // runs every 20 seconds.
    }

    /** Stops all operations of the manager by stopping the {@link #heartbeatTask} and clearing the {@link #TRACKED_BATTLES} map. */
    public void stop() {
        if (heartbeatTask != null) {
            heartbeatTask.cancel();
            heartbeatTask = null;
        }

        TRACKED_BATTLES.clear();
    }

    /** Retrieves all battles from the {@link #DATABASE} to be resumed to support persistence beyond restarts and crashes. */
    private void populateBattles() {
        DATABASE.getTrackedBattles().thenAcceptAsync(trackedBattleResults -> {
            for (var tbr : trackedBattleResults) {
                TRACKED_BATTLES.put(tbr.townName(), new TrackedBattle(tbr));
            }
        }, runnable -> Bukkit.getScheduler().runTask(PLUGIN, runnable))
            .exceptionally(ex -> {
                ex.printStackTrace();
                return null;
            });
    }
}
