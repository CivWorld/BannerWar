package io.github.townyadvanced.flagwar;

import io.github.townyadvanced.flagwar.database.DatabaseService;
import io.github.townyadvanced.flagwar.objects.Battle;
import io.github.townyadvanced.flagwar.objects.BattleRecord;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class BattleManager {

    /** Holds the {@link DatabaseService} instance. */
    private final DatabaseService DATABASE_SERVICE;

    /** Holds the {@link JavaPlugin} instance. */
    private final JavaPlugin PLUGIN;

    /** Holds a {@link HashMap} of every {@link Battle} and its associated contested town's name. */
    private static final Map<String, Battle> ACTIVE_BATTLES =  new HashMap<>();


    public BattleManager(JavaPlugin plugin, DatabaseService databaseService) {
        DATABASE_SERVICE = databaseService;
        PLUGIN = plugin;
        resumeBattles();
    }

    private void resumeBattles() {
        DATABASE_SERVICE.getBattles().thenAccept(battleRecords -> {
            for (BattleRecord r : battleRecords) {
                ACTIVE_BATTLES.put(r.contestedTown(), new Battle(r));
            }
        });
    }

    public void updateActiveBattles() {
        for (Map.Entry<String, Battle> entry : ACTIVE_BATTLES.entrySet()) {
            DATABASE_SERVICE.updateBattle(BattleRecord.of(entry.getValue()));
        }
    }
}
