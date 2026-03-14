package io.github.townyadvanced.flagwar.config;

import io.github.townyadvanced.flagwar.FlagWar;
import io.github.townyadvanced.flagwar.objects.BattleStage;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;

import java.time.Duration;
import java.util.Locale;
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

    public static void incrementTownyDay() {
        PLUGIN.getConfig().set("universe.current_day", getCurrentTownyDay() + 1);
    }

    public static int getExtraFlagLives() {
        return PLUGIN.getConfig().getInt("flag_lives.count");
    }

    public static int getFlagLifeTimeIncrease() {
        return PLUGIN.getConfig().getInt("flag_lives.lifetime_increase");
    }

    public static int getCycleSpeedSeconds() {
        return PLUGIN.getConfig().getInt("clock.cycle_speed");
    }

    /**
     * Returns the {@link Material} parsed from the string at the plugin's YAML configuration file.
     * @param dir the directory where the {@link Material} resides
     * @param defaultMat the default {@link Material} to be returned if the directory contains an empty string or null
     * @throws IllegalArgumentException the directory may contain a value that cannot be parsed to a {@link Material}.
     */
    private static Material getMaterial(String dir, Material defaultMat) throws IllegalArgumentException {
        String matString = PLUGIN.getConfig().getString(dir);

        if (matString == null || matString.isEmpty()) return defaultMat;

        return Material.valueOf(matString.toUpperCase(Locale.ROOT));
    }

    public static Material getFlagLifePaymentItem() {
        Material m;

        try {
            m = getMaterial("flag_lives.item_of_payment", Material.GOLD_INGOT);
        } catch (IllegalArgumentException e) {
            LOGGER.warning("Invalid item of payment material. Using gold ingot instead.");
            return Material.GOLD_INGOT;
        }

        return m;
    }

    public static Material getInfernalWarFlagMaterial() {
        Material m;

        try {
            m = getMaterial("civics.civtechs.infernal_warflags.material", Material.ANCIENT_DEBRIS);
        } catch (IllegalArgumentException e) {
            LOGGER.warning("Invalid infernal war flag material. Using ancient debris instead.");
            return Material.ANCIENT_DEBRIS;
        }

        return m;
    }

    public static int getCaesarCipherDelay() {
        return PLUGIN.getConfig().getInt("civics.civtechs.caesar_cipher.delay");
    }

    public static int getAttritionFlagLifeTimeIncrease() {
        return PLUGIN.getConfig().getInt("civics.civtechs.attrition_doctrine.lifetime_increase");
    }

    public static int getInfernalLifeTimeIncrease() {
        return PLUGIN.getConfig().getInt("civics.civtechs.infernal_warflags.lifetime_increase");
    }

    public static int getFlagLifePrice(int n) {
        int base = PLUGIN.getConfig().getInt("flag_lives.base_price");
        return (int) (base * Math.round(Math.pow(2, n))); // 2 ^ n is a bitwise operation, so use Math.pow().
    }

    /**
     * Returns the integer value at the specified directory path, where the root is the war weariness percentage values.
     * @param dir the specified directory path
     */
    private static int getFromWeariness(String dir) {
        return PLUGIN.getConfig().getInt("civics.war_weariness." + dir);
    }

    public static int getBaseWearinessValue() {
        return getFromWeariness("base_price");
    }

    public static int getFlagPlaceAttackerIncrease() {
        return getFromWeariness("flag_place.attacker_increase");
    }

    private static int getFlagDefendWeariness(String dir) {
        return getFromWeariness("flag_defend." + dir);
    }

    private static int getBattleDefenseWon(String dir) {
        return getFromWeariness("defense_won." + dir);
    }

    private static int getBattleDefenseLost(String dir) {
        return getFromWeariness("defense_lost." + dir);
    }

    public static int getFlagDefendAttackerWeariness() {
        return getFlagDefendWeariness("attacker_increase");
    }

    public static int getFlagDefendDefenderWeariness() {
        return getFlagDefendWeariness("defender_decrease");
    }

    public static int getFlagWinAttackerIncrease() {
        return getFromWeariness("flag_win.attacker_increase");
    }

    public static int getDefenseWonAttackerIncrease(boolean isAutocracy) {
        if (isAutocracy)
            return getBattleDefenseWon("attacker.autocracy_increase");

        else
            return getBattleDefenseWon("attacker.non_autocracy_increase");

    }

    public static int getDefenseWonDefenderDecrease(boolean isAutocracy) {
        if (isAutocracy)
            return getBattleDefenseWon("defender.autocracy_decrease");

        else
            return getBattleDefenseWon("defender.non_autocracy_decrease");

    }

    public static int getDefenseLostAttackerDecrease(boolean isAutocracy) {
        if (isAutocracy)
            return getBattleDefenseWon("attacker.autocracy_decrease");

        else
            return 0; // no set config values for non-autocracy decrease

    }

    public static int getDefenseLostDefenderIncrease(boolean isAutocracy) {
        if (isAutocracy)
            return getBattleDefenseLost("defender.autocracy_increase");

        else
            return getBattleDefenseLost("defender.non_autocracy_increase");

    }

    public static int getTownLeaveWearinessThreshold() {
        return getFromWeariness("town_leave_threshold");
    }

    public static int getNewDayWearinessDecrease() {
        return getFromWeariness("new_day.guaranteed_decrease");
    }

    public static int getNewDayExpiredDecrease() {
        return getFromWeariness("new_day.expired_banner_placer_decrease");
    }

    public static int getDaysUntilBannerPlacerExpired() {
        return getFromWeariness("new_day.days_until_expired");
    }

    public static double getWarEconomyDecrease() {
        return PLUGIN.getConfig().getInt("civics.civtechs.war_economy.percentage_weariness_decrease");
    }

    public static double getTimeMultiplier(BattleStage stage) {
        double out = PLUGIN.getConfig().getDouble("battle.timing_multipliers." + stage.name().toLowerCase(Locale.ROOT));

        if (out <= 0) {
            LOGGER.warning("Configured multiplier " + out + " for battle stage " + stage.name() + " not suitable! Returning 1.0 instead.");
            return 1.0;
        }

        return out;
    }

    public static Material getInvincibilityMaterial() {
        Material m;

        try {
            m = getMaterial("flag_lives.invincibility_material", Material.BEDROCK);
        } catch (IllegalArgumentException e) {
            LOGGER.warning("Invalid invincible flag material. Using bedrock instead.");
            return Material.ANCIENT_DEBRIS;
        }

        return m;

    }

    public static long getInvincibilityDuration() {
        return PLUGIN.getConfig().getLong("flag_lives.invincibility_duration");
    }

    public static Duration getTimeUntilNoMoreLives() {
        return Duration.ofSeconds(PLUGIN.getConfig().getLong("flag_lives.time_until_no_more_lives"));
    }
}
