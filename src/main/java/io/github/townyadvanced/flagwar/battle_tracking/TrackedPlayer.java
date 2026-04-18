package io.github.townyadvanced.flagwar.battle_tracking;

import io.github.townyadvanced.flagwar.battle_tracking.structures.enums.Affiliation;
import io.github.townyadvanced.flagwar.battle_tracking.structures.occurrences.FlagOccurrence;
import io.github.townyadvanced.flagwar.battle_tracking.structures.occurrences.KillOccurrence;
import io.github.townyadvanced.flagwar.battle_tracking.structures.results.PlayerResult;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.potion.PotionType;

import java.util.*;

public class TrackedPlayer {
    private final OfflinePlayer PLAYER;
    private final Affiliation AFFILIATION; // the issue of players switching teams mid-battle comes up
    // recalculate affiliation at the end of the battle?

    private double damageDealt;
    private double damageTaken;

    private final Collection<KillOccurrence> KILLS;
    private final Collection<KillOccurrence> DEATHS;
    private final Collection<FlagOccurrence> FLAGS;

    private final Map<Material, Integer> CONSUMED_ITEMS;
    private final Map<PotionType, Integer> CONSUMED_POTIONS;

    private TrackedPlayer(
        OfflinePlayer p, Affiliation affiliation, double damageDealt, double damageTaken, Collection<KillOccurrence> kills, Collection<KillOccurrence> deaths, Collection<FlagOccurrence> flags, int potsUsed, int gapsUsed)
    {
        this.PLAYER = p;
        this.AFFILIATION = affiliation;
        this.damageDealt = damageDealt;
        this.damageTaken = damageTaken;
        this.KILLS = kills;
        this.DEATHS = deaths;
        this.FLAGS = flags;
        this.CONSUMED_ITEMS = new EnumMap<>(Material.class);
        this.CONSUMED_POTIONS = new EnumMap<>(PotionType.class);
        this.CONSUMED_ITEMS.put(Material.ENCHANTED_GOLDEN_APPLE, gapsUsed);
        this.CONSUMED_POTIONS.put(PotionType.INSTANT_HEAL, potsUsed);
    }
    public TrackedPlayer(OfflinePlayer p, Affiliation affiliation) {
        this(
            p,
            affiliation,
            0,
            0,
            new ArrayDeque<>(),
            new ArrayDeque<>(),
            new ArrayDeque<>(),
            0,
            0);
    }

    public TrackedPlayer(PlayerResult result) {
        this(
            Bukkit.getOfflinePlayer(result.playerName()),
            result.affiliation(),
            result.damageDealt(),
            result.damageTaken(),
            result.kills(),
            result.deaths(),
            result.flags(),
            result.potsUsed(),
            result.gapsUsed()
        );
    }

    public static Map<UUID, TrackedPlayer> fromMap(Map<String, PlayerResult> map) {
        Map<UUID, TrackedPlayer> out = new HashMap<>();
        for (var item : map.values()) {
            var newTracked = new TrackedPlayer(item);
            out.put(newTracked.getOfflinePlayer().getUniqueId(), newTracked);
        }
        return out;
    }

    public void incrementKills(KillOccurrence killOccurrence) {
        KILLS.add(killOccurrence);
    }

    public void incrementDeaths(KillOccurrence killOccurrence) {
        DEATHS.add(killOccurrence);
    }

    public void addDamageDealt(double damageDealt) {
        this.damageDealt += damageDealt;
    }

    public void addDamageTaken(double damageTaken) {
        this.damageTaken += damageTaken;
    }

    public void registerConsumedItem(Material item) {
        CONSUMED_ITEMS.put(item, CONSUMED_ITEMS.getOrDefault(item, 0) + 1);
    }

    public void registerConsumedPotion(PotionType type) {
        CONSUMED_POTIONS.put(type, CONSUMED_POTIONS.getOrDefault(type, 0) + 1);
    }

    public void registerFlag(FlagOccurrence flagOccurrence) {
        FLAGS.add(flagOccurrence);
    }

    public OfflinePlayer getOfflinePlayer() {
        return PLAYER;
    }

    public Affiliation getAffiliation() {
        return AFFILIATION;
    }

    public double getDamageDealt() {
        return damageDealt;
    }

    public double getDamageTaken() {
        return damageTaken;
    }

    public int getPotionsUsed(PotionType type) {
        return CONSUMED_POTIONS.getOrDefault(type, 0);
    }

    public int getItemsUsed(Material type) {
        return CONSUMED_ITEMS.getOrDefault(type, 0);
    }

    public Collection<KillOccurrence> getKills() {
        return KILLS;
    }

    public Collection<KillOccurrence> getDeaths() {
        return DEATHS;
    }

    public Collection<FlagOccurrence> getFlagLogs() {
        return FLAGS;
    }
}
