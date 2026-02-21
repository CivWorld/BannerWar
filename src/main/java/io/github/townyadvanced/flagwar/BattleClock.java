package io.github.townyadvanced.flagwar;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.logging.Logger;

public final class BattleClock {

    /** Holds the {@link JavaPlugin} instance. */
    private final JavaPlugin PLUGIN;

    /** Holds the {@link BattleManager} instance.*/
    private final BattleManager BATTLE_MANAGER;

    /** Holds the {@link org.bukkit.scheduler.BukkitScheduler} instance. */
    private final BukkitScheduler SCHEDULER = Bukkit.getScheduler();

    /** Holds the {@link BukkitTask} that runs every configurable cycle. The duration between cycles defaults to 2 minutes. */
    private BukkitTask CLOCK_TASK;

    /** Holds the {@link Logger} of this class. */
    private final Logger LOGGER;

    public BattleClock(JavaPlugin plugin, BattleManager manager) {
        this.PLUGIN = plugin;
        LOGGER = PLUGIN.getLogger();
        BATTLE_MANAGER = manager;
        start();
    }

    /**
     * Initiates the clock task and all its periodic operations.
     */
    private void start() {
        CLOCK_TASK = SCHEDULER.runTaskTimer(
            PLUGIN,
            () -> {
                onCycle();
            },
            200,
            200); // TODO: MAKE THIS CONFIGURABLE
    }

    /**
     * Ends the clock task and all its periodic operations.
     * */
    public void kill() {
        if (CLOCK_TASK != null) {
            CLOCK_TASK.cancel();
            CLOCK_TASK = null;
        }
    }

    /**
     * The tasks that this class will perform every configured cycle.
     */
    private void onCycle() {
        BATTLE_MANAGER.updateBattles();
    }
}
