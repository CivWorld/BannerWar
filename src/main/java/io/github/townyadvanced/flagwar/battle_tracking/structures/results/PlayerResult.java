package io.github.townyadvanced.flagwar.battle_tracking.structures.results;

import io.github.townyadvanced.flagwar.battle_tracking.TrackedPlayer;
import io.github.townyadvanced.flagwar.battle_tracking.structures.enums.Affiliation;
import io.github.townyadvanced.flagwar.battle_tracking.structures.occurrences.FlagOccurrence;
import io.github.townyadvanced.flagwar.battle_tracking.structures.occurrences.KillOccurrence;
import org.bukkit.Material;
import org.bukkit.potion.PotionType;

import java.util.Collection;

public record PlayerResult(
    String playerName,
    Affiliation affiliation,
    Collection<KillOccurrence> kills,
    Collection<KillOccurrence> deaths,
    double damageDealt,
    double damageTaken,
    int potsUsed,
    int gapsUsed,
    Collection<FlagOccurrence> flags
    )
{
    public static PlayerResult parse(TrackedPlayer player) {
        String name = player.getOfflinePlayer().getName();

        var kills = player.getKills();
        var deaths = player.getDeaths();

        int potsUsed = player.getPotionsUsed(PotionType.INSTANT_HEAL);
        int gapsUsed = player.getItemsUsed(Material.ENCHANTED_GOLDEN_APPLE);

        return new PlayerResult(
            name,
            player.getAffiliation(),
            kills,
            deaths,
            player.getDamageDealt(),
            player.getDamageTaken(),
            potsUsed,
            gapsUsed,

            player.getFlagLogs()
        );
    }
}
