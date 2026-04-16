package io.github.townyadvanced.flagwar.database;

import io.github.townyadvanced.flagwar.FlagWar;

import java.util.logging.Logger;

public class TrackerDatabase {
    /** Holds the name of the battle table. */
    private static final String TRACKED_BATTLE_TABLE = "TrackedBattle";

    /** Holds the name of the banner placer table. */
    private static final String TRACKED_PLAYER_TABLE = "TrackedPlayer";

    /** Holds the {@link DatabaseManager} instance. */
    private final DatabaseManager MANAGER;

    /** Holds the {@link Logger} of this class. */
    private final Logger LOGGER = FlagWar.getInstance().getLogger();

    public TrackerDatabase(DatabaseManager manager) {
        this.MANAGER = manager;
    }


}
