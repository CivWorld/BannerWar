package io.github.townyadvanced.flagwar.battle_tracking;

import com.palmergames.bukkit.towny.object.Town;
import io.github.townyadvanced.flagwar.FlagWar;
import io.github.townyadvanced.flagwar.battle_tracking.structures.enums.BattleResultEnum;
import io.github.townyadvanced.flagwar.battle_tracking.structures.results.TrackedBattleResult;
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

public final class TrackedBattleManager {

    private final Map<String, TrackedBattle> TRACKED_BATTLES = new HashMap<>();
    private BukkitTask heartbeatTask;
    private final TrackerDatabase DATABASE;
    private final Plugin PLUGIN = FlagWar.getInstance();

    public TrackedBattle getBattleAt(Location location) {
        return getBattleAt(location.toVector());
    }

    public TrackedBattle getBattleAt(Vector position) {
        for (var battle : TRACKED_BATTLES.values()) {
            if (battle.isInBattleRegion(position)) return battle;
        }
        return null;
    }

    public void trackBattle(Battle battle) {
        Town town = battle.getContestedTown();
        TRACKED_BATTLES.put(town.getName(), new TrackedBattle(town, battle.getAttacker(), battle.getDefender()));
    }

    public TrackedBattleManager(TrackerDatabase database) {
        this.DATABASE = database;
        // HACK: stand by for 8 seconds to allow for all asynchronous loading to occur.
        // could have the battle tracker be restored upon BattleResumeEvent but can't be bothered to implement.
        Bukkit.getScheduler().runTaskLater(PLUGIN, this::start, 160);
    }

    public void start() {
        populateBattles();

        heartbeatTask = Bukkit.getScheduler().runTaskTimer(PLUGIN,
            () -> {
                for (var TB : TRACKED_BATTLES.values()) {
                    var result = TrackedBattleResult.parse(TB, BattleResultEnum.ONGOING);
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

    public void stop() {

        TRACKED_BATTLES.clear();

        if (heartbeatTask != null) {
            heartbeatTask.cancel();
            heartbeatTask = null;
        }
    }

    private void populateBattles() {
        System.out.println("Populating Battles");
        DATABASE.getTrackedBattles().thenAcceptAsync(trackedBattleResults -> {
            System.out.println("TRACKED BATTLES " + trackedBattleResults);
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
