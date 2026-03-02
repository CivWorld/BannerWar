package io.github.townyadvanced.flagwar;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import io.github.townyadvanced.flagwar.database.DatabaseInteraction;
import io.github.townyadvanced.flagwar.events.BattleStartEvent;
import io.github.townyadvanced.flagwar.objects.BannerPlacerRecord;
import io.github.townyadvanced.flagwar.objects.Battle;
import io.github.townyadvanced.flagwar.objects.BattleRecord;
import io.github.townyadvanced.flagwar.util.BattleUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class BattleManager {

    /** Holds the {@link DatabaseInteraction} instance. */
    private final DatabaseInteraction DATABASE_INTERACTION;

    /** Holds the {@link JavaPlugin} instance. */
    private final JavaPlugin PLUGIN;

    /** Holds a {@link HashMap} of every {@link Battle} and its associated contested town's name. */
    private static final Map<String, Battle> ACTIVE_BATTLES = new HashMap<>();

    public BattleManager(JavaPlugin plugin, DatabaseInteraction databaseInteraction) {
        DATABASE_INTERACTION = databaseInteraction;
        PLUGIN = plugin;
        resumeBattles();
    }

    /**
     * Starts the last saved {@link Battle}s from the database.
     */
    private void resumeBattles() {
        ACTIVE_BATTLES.clear();

        DATABASE_INTERACTION.getBattles().thenAccept(battleRecords -> {
            for (BattleRecord r : battleRecords) {
                ACTIVE_BATTLES.put(r.contestedTown(), new Battle(r));
                PLUGIN.getLogger().info("Battle " + r.contestedTown() + " has been resumed");
            }
        });
    }

    /**
     * Returns every currently ongoing battle, dormant or not.
     */
    public static Collection<Battle> getActiveBattles() {
        return ACTIVE_BATTLES.values();
    }

    /**
     * Refreshes the battles' states, and saves them to the database.
     */
    public void updateBattles() {
        for (Map.Entry<String, Battle> entry : ACTIVE_BATTLES.entrySet()) {
            Battle battle = entry.getValue();

            if (battle.isPendingStageAdvance()) battle.advanceStage(true);

            battle.updateBossBar();

            DATABASE_INTERACTION.insertOrUpdate(BattleRecord.of(battle));
        }
    }

    /**
     * Begins a battle.
     * @param contestedTown the {@link Town} where the {@link Battle} is hosted
     * @param attacker the nation that initiated the {@link Battle}
     * @param defender the nation that houses the {@link Town} where the {@link Battle} is hosted
     */
    public static void startBattle(Town contestedTown, Nation attacker, Nation defender) {

        Battle battle = new Battle(attacker, defender, contestedTown, false);
        ACTIVE_BATTLES.put(contestedTown.getName(), battle);
        System.out.println("Battle to be backed up by nonexistent chunk backing up functions.");

        Bukkit.getServer().getPluginManager().callEvent(new BattleStartEvent(battle));
    }

    /**
     * Returns the {@link Battle} associated by the specified town name.
     * @param townName the specified town name
     */
    public static Battle getBattle(String townName) {
        return ACTIVE_BATTLES.getOrDefault(townName, null);
    }

    /**
     * Removes the {@link Battle} from the {@link #ACTIVE_BATTLES} map and the database.
     * @param battle the specified {@link Battle}
     */
    public static void removeBattle(Battle battle) {
        ACTIVE_BATTLES.remove(battle.getContestedTown().getName());
    }

    /**
     * Removes the {@link Battle} from the {@link #ACTIVE_BATTLES} map.
     * @param town the specified {@link Battle}'s contested town.
     */
    public static void removeBattle(Town town) {
        ACTIVE_BATTLES.remove(town.getName());
    }

    /**
     * Removes the {@link Battle} from the {@link #ACTIVE_BATTLES} map.
     * @param townName the specified {@link Battle}'s contested town's name.
     */
    public static void removeBattle(String townName) {
        ACTIVE_BATTLES.remove(townName);
    }

    /**
     * Removes the boss bar of every {@link Battle} to prevent duplicates.
     */
    public static void deleteBossBars() {
        for (var battle : ACTIVE_BATTLES.values()) battle.deleteBossBar();

    }

    public void logBannerPlacer(BannerPlacerRecord bannerPlacerRecord) {
        DATABASE_INTERACTION.insertOrUpdate(bannerPlacerRecord);
    }

    /**
     * Returns every {@link Town} whose last banner placement happened a number of days ago that is greater than or equal to the specified number of days.
     * @param days the specified number of days
     */
    public CompletableFuture<Collection<Town>> getAllExpiredBannerPlacers(long days) {
        return DATABASE_INTERACTION.getBannerPlacers().thenApply(bannerPlacerRecords -> {

            Collection<Town> out = new ArrayList<>();
            bannerPlacerRecords.forEach(bp -> {
                if (BattleUtil.daysSince(bp.dayOfAttack()) >= days) out.add(bp.town());
            });
            return out;
        });
    }
}
