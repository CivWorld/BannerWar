package io.github.townyadvanced.flagwar;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import org.bukkit.entity.Player;
import org.flintstqne.townyCivics.CivTech.CivTech;
import org.flintstqne.townyCivics.CivTech.CivTechRarity;
import org.flintstqne.townyCivics.CivTech.CivTechType;
import org.flintstqne.townyCivics.api.TownyCivicsAPI;

public class CivTechs {


    private CivTechs() {}

    /** Holds the ID of the Caesar cipher {@link CivTech}. */
    public static final String CAESAR_CIPHER = "bannerwar_caesar_cipher";

    /** Holds the ID of the attrition doctrine {@link CivTech}. */
    public static final String ATTRITION_DOCTRINE = "bannerwar_attrition_doctrine";

    /** Holds the ID of the war economy {@link CivTech}. */
    public static final String WAR_ECONOMY = "bannerwar_war_economy";

    /** Holds the ID of the infernal war flags {@link CivTech}. */
    public static final String INFERNAL_WARFLAGS = "bannerwar_infernal_warflags";


    public static void registerCivTechs() {
        var api = getAPI();
        if (api == null) return;

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

    public static TownyCivicsAPI getAPI() {
        return TownyCivicsAPI.getInstance();
    }

    public static boolean isTechPresent(String ID, Resident r) {
        var api = getAPI();
        if (api == null || r == null || r.getPlayer() == null || r.getTownOrNull() == null) return false;

        return api.isCivTechActive(r.getPlayer(), ID) || api.isCivTechActiveForTown(r.getTownOrNull(), ID);
    }

    public static boolean isTechPresent(String ID, Player p) {
        return isTechPresent(ID, TownyAPI.getInstance().getResident(p.getUniqueId()));
    }

    public static boolean isTechPresent(String ID, String name) {
        return isTechPresent(ID, TownyAPI.getInstance().getResident(name));
    }
}
