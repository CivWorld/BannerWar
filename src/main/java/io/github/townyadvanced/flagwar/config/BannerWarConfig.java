package io.github.townyadvanced.flagwar.config;

import io.github.townyadvanced.flagwar.FlagWar;
import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

public class BannerWarConfig {

    private BannerWarConfig() {}

    /** {@link Plugin} instance, used internally. */
    private static final Plugin PLUGIN = FlagWar.getInstance();

    /** Holds an instance of FlagWar's logger. */
    private static final Logger LOGGER = PLUGIN.getLogger();

    public static long getCurrentTownyDay() {
        return PLUGIN.getConfig().getLong("universe.current_day");
    }

    public static void incrementTownyDay() {PLUGIN.getConfig().set("universe.current_day", getCurrentTownyDay() + 1);}

}
