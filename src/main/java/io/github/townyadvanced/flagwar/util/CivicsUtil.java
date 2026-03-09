package io.github.townyadvanced.flagwar.util;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import io.github.townyadvanced.flagwar.FlagWar;
import io.github.townyadvanced.flagwar.config.BannerWarConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.flintstqne.townyCivics.CivTech.CivTech;
import org.flintstqne.townyCivics.CivTech.CivTechRarity;
import org.flintstqne.townyCivics.CivTech.CivTechType;
import org.flintstqne.townyCivics.api.TownyCivicsAPI;

public final class CivicsUtil {

    private CivicsUtil() {}

    /** Holds the {@link TownyCivicsAPI} that this class interacts with. */
    private static TownyCivicsAPI api;

    /** Holds the {@link Plugin} instance. */
    public static final Plugin PLUGIN = FlagWar.getInstance();

    /** Holds the ID of the name of the war weariness upkeep modifier. */
    public static final String WEARINESS = "War Weariness";

    public static final String FORMATTED_WEARINESS = "War Weariness (%d%%)";

    /** Holds the ID of the Caesar cipher {@link CivTech}. */
    public static final String CAESAR_CIPHER = "bannerwar_caesar_cipher";

    /** Holds the ID of the attrition doctrine {@link CivTech}. */
    public static final String ATTRITION_DOCTRINE = "bannerwar_attrition_doctrine";

    /** Holds the ID of the war economy {@link CivTech}. */
    public static final String WAR_ECONOMY = "bannerwar_war_economy";

    /** Holds the ID of the infernal war flags {@link CivTech}. */
    public static final String INFERNAL_WARFLAGS = "bannerwar_infernal_warflags";

    /** Registers the {@link CivTech}s related to BannerWar. */
    public static void registerCivTechs() {

        api.registerCivTech(
            ATTRITION_DOCTRINE, "Attrition Doctrine",
            "Doctrine which delays your enemies' advance into your town. The attacker's flag life is extended by 10 seconds.", CivTechType.WAR, CivTechRarity.RARE);

        api.registerCivTech(
            CAESAR_CIPHER, "Caesar Cipher",
            "Encryption techniques to prevent the enemy from discovering your flag as quickly.", CivTechType.WAR, CivTechRarity.UNCOMMON);

        api.registerCivTech(
            WAR_ECONOMY, "War Economy",
            "Economic theory on wartime economies.", CivTechType.WAR, CivTechRarity.RARE);

        api.registerCivTech(
            INFERNAL_WARFLAGS, "Infernal War Flags",
            "Imbue your war flag with ancient netherite to significantly harden its second life.", CivTechType.WAR, CivTechRarity.EPIC);
    }

    /** Unregisters the {@link CivTech}s related to BannerWar. */
    public static void unRegisterCivTechs() {

        api.unregisterCivTech(ATTRITION_DOCTRINE);
        api.unregisterCivTech(WAR_ECONOMY);
        api.unregisterCivTech(INFERNAL_WARFLAGS);
        api.unregisterCivTech(CAESAR_CIPHER);
    }

    /** Initialization steps such as getting the {@link #api} and registering the {@link CivTech}s.  */
    public static void init() {
        Bukkit.getScheduler().runTaskLater(PLUGIN, () -> {
            api = TownyCivicsAPI.getInstance();
            registerCivTechs();
        }, 1);
    }

    /**
     * Returns whether the specified {@link CivTech} is present for this {@link Resident}'s self, town or nation.
     *
     * @param ID the ID of the {@link CivTech}
     * @param r  the {@link Resident} in question
     */
    public static boolean isTechPresent(String ID, Resident r) {
        if ( r == null || r.getPlayer() == null || r.getTownOrNull() == null) return false;

        return api.isCivTechActive(r.getPlayer(), ID) || api.isCivTechActiveForTown(r.getTownOrNull(), ID);
    }

    /**
     * Returns whether the specified {@link CivTech} is present for this {@link Player}'s {@link Resident}'s self, town or nation.
     *
     * @param ID the ID of the {@link CivTech}
     * @param p  the {@link Player} in question
     */
    public static boolean isTechPresent(String ID, Player p) {
        return isTechPresent(ID, TownyAPI.getInstance().getResident(p.getUniqueId()));
    }

    /**
     * Returns whether the specified {@link CivTech} is present for this {@link Resident}'s self, town or nation.
     *
     * @param ID   the ID of the {@link CivTech}
     * @param name the name of the {@link Resident} in question
     */
    public static boolean isTechPresent(String ID, String name) {
        return isTechPresent(ID, TownyAPI.getInstance().getResident(name));
    }

    public static boolean isAutocracy(Nation nation) {
        return nation != null && api.isAutocracy(nation);
    }

    public static boolean isFederation(Nation nation) {
        return nation != null && api.isFederation(nation);
    }

    public static void increaseWeariness(Town town, double d) {
        changeTownWeariness(town, d);
    }

    public static void increaseWeariness(Resident resident, double d) {

        if (resident == null || resident.getTownOrNull() == null || resident.getTownOrNull().getNationOrNull() == null) {
            PLUGIN.getLogger().warning("Cannot increase weariness by " + d + " because their town or nation is null!");
            return;
        }

        double decimalDecrease = BannerWarConfig.getWarEconomyDecrease() / 100d;
        if (isTechPresent(WAR_ECONOMY, resident)) d = d * decimalDecrease;

        Town town = resident.getTownOrNull();
        Nation nation = town.getNationOrNull();

        if (api.isFederation(nation)) increaseWeariness(town, d);
        else increaseWeariness(nation, d);
    }

    public static void decreaseWeariness(Resident resident, double d) {
        if (resident == null || resident.getTownOrNull() == null || resident.getTownOrNull().getNationOrNull() == null) {
            PLUGIN.getLogger().warning("Cannot decrease weariness by " + d + " because their town or nation is null!");
            return;
        }

        Town town = resident.getTownOrNull();
        Nation nation = town.getNationOrNull();

        if (api.isFederation(nation)) decreaseWeariness(town, d);
        else decreaseWeariness(nation, d);
    }

    public static void decreaseWeariness(Town town, double d) {
        changeTownWeariness(town, -d);
    }

    public static void increaseWeariness(Nation nation, double d) {
        changeNationWeariness(nation, d);
    }

    public static void decreaseWeariness(Nation nation, double d) {
        changeNationWeariness(nation, -d);
    }

    public static double getWeariness(Town town) {
        if (town == null) return 0.0;

        for (var set : api.getUpkeepModifiers(town).entrySet()) {
            String name  = set.getKey();
            double value = set.getValue();

            if (name.contains(WEARINESS)) return value;
        }

        return 0.0;
    }

    public static double getWeariness(Nation nation) {
        if (nation == null) return 0.0;

        for (var set : api.getNationUpkeepModifiers(nation).entrySet()) {
            String name  = set.getKey();
            double value = set.getValue();

            if (name.contains(WEARINESS)) return value;
        }

        return 0.0;    }

    public static double getWearinessAsPercentage(Town town) {
        return getWeariness(town)/BannerWarConfig.getBaseWearinessValue();
    }

    public static double getWearinessAsPercentage(Nation nation) {
        return getWeariness(nation)/BannerWarConfig.getBaseWearinessValue();
    }

    private static void changeTownWeariness(Town town, double d) {
        double h = d*BannerWarConfig.getBaseWearinessValue();

        if (town == null) {
            PLUGIN.getLogger().warning("Cannot change weariness by " + h + " because town is null!");
            return;
        }

        double initialValue = getWeariness(town);
        double finalValue = Math.max(initialValue + h, 0);

        String oldName = String.format(FORMATTED_WEARINESS, Math.round(toPercentage(initialValue)));
        String newName = String.format(FORMATTED_WEARINESS, Math.round(toPercentage(finalValue)));

        api.removeUpkeepModifier(town, oldName);
        api.addUpkeepModifier(town, newName, finalValue);
    }

    private static void changeNationWeariness(Nation nation, double d) {
        double h = d*BannerWarConfig.getBaseWearinessValue();

        if (nation == null) {
            PLUGIN.getLogger().warning("Cannot change weariness by " + h + " because nation is null!");
            return;
        }

        double initialValue = getWeariness(nation);
        double finalValue = Math.max(initialValue + h, 0);

        String oldName = String.format(FORMATTED_WEARINESS, Math.round(toPercentage(initialValue)));
        String newName = String.format(FORMATTED_WEARINESS, Math.round(toPercentage(finalValue)));

        api.removeNationUpkeepModifier(nation, oldName);
        api.addNationUpkeepModifier(nation, newName, finalValue);
    }

    private static double toPercentage(double d) {
        return d/BannerWarConfig.getBaseWearinessValue();
    }
}
