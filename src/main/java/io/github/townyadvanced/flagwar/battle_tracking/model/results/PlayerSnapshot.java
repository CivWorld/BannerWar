package io.github.townyadvanced.flagwar.battle_tracking.model.results;

import io.github.townyadvanced.flagwar.battle_tracking.TrackedPlayer;
import io.github.townyadvanced.flagwar.battle_tracking.model.enums.Affiliation;
import io.github.townyadvanced.flagwar.battle_tracking.model.occurrences.FlagOccurrence;
import io.github.townyadvanced.flagwar.battle_tracking.model.occurrences.KillOccurrence;
import org.bukkit.Material;
import org.bukkit.potion.PotionType;

import java.util.Collection;

/**
 * A record that logs a serialized snapshot of all tracked encounters and actions of a player during a battle.
 * @param playerName the name of the player
 * @param affiliation the affiliation of this player in the battle
 * @param kills a collection of all kill events they are responsible for since the start of this battle
 * @param deaths a collection of all kill events they fell victim in since the start of this battle
 * @param damageDealt the total damage dealt by this player since the start of this battle
 * @param damageTaken the total damage taken by this player since the start of this battle
 * @param potsUsed the number of healing potions used by this player since the start of this battle
 * @param gapsUsed the number of enchanted golden apples used by this player since the start of this battle
 * @param flags a collection of all flag events that they played a part in (placed and won, or destroyed)
 */
public record PlayerSnapshot(
    String playerName,
    Affiliation affiliation,
    Collection<KillOccurrence> kills,
    Collection<KillOccurrence> deaths,
    double damageDealt,
    double damageTaken,
    int potsUsed,
    int gapsUsed,
    Collection<FlagOccurrence> flags
) {
    /**
     * Returns a {@link PlayerSnapshot} of a {@link TrackedPlayer} to be stored on a database or similar.
     * @param player the tracked player
     */
    public static PlayerSnapshot parse(TrackedPlayer player) {
        String name = player.getOfflinePlayer().getName();

        var kills = player.getKills();
        var deaths = player.getDeaths();

        int potsUsed = player.getPotionsUsed(PotionType.INSTANT_HEAL);
        int gapsUsed = player.getItemsUsed(Material.ENCHANTED_GOLDEN_APPLE);

        return new PlayerSnapshot(
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
