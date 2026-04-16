package io.github.townyadvanced.flagwar.battle_tracking.structures.results;

import io.github.townyadvanced.flagwar.battle_tracking.TrackedPlayer;
import io.github.townyadvanced.flagwar.battle_tracking.structures.enums.Affiliation;
import io.github.townyadvanced.flagwar.battle_tracking.structures.enums.FlagResult;
import io.github.townyadvanced.flagwar.battle_tracking.structures.occurrences.KillOccurrence;
import org.bukkit.Material;
import org.bukkit.potion.PotionType;

import java.util.Collection;
import java.util.function.Predicate;

public record PlayerResult(
    String playerName,
    Affiliation affiliation,
    Collection<KillOccurrence> kills,
    Collection<KillOccurrence> deaths,
    int potsUsed,
    int gapsUsed,
    int flagsPlaced,
    int flagsWon,
    int flagsBroken
)
{
    public static PlayerResult parse(TrackedPlayer player) {
        String name = player.getOfflinePlayer().getName();

        var kills = player.getKills();
        var deaths = player.getDeaths();

        int potsUsed = player.getPotionsUsed(PotionType.INSTANT_HEAL);
        int gapsUsed = player.getItemsUsed(Material.ENCHANTED_GOLDEN_APPLE);

        var flagsPlaced = player.getFlagLogs().stream().filter(FO -> FO.flagPlacer().equals(name)).toList();
        var flagsWon = flagsPlaced.stream().filter(FO -> FO.result().equals(FlagResult.FLAG_SUCCESS)).toList();
        var flagsBroken = flagsPlaced.stream().filter(Predicate.not(flagsWon::contains)).toList();

        return new PlayerResult(
            name,
            player.getAffiliation(),
            kills,
            deaths,
            potsUsed,
            gapsUsed,

            // change accordingly if you wish to display more descriptive flag information.
            flagsPlaced.size(),
            flagsWon.size(),
            flagsBroken.size()
        );
    }
}
