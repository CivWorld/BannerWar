package io.github.townyadvanced.flagwar.battle_tracking;

import io.github.townyadvanced.flagwar.battle_tracking.model.enums.Affiliation;
import io.github.townyadvanced.flagwar.battle_tracking.model.occurrences.FlagOccurrence;
import io.github.townyadvanced.flagwar.battle_tracking.model.occurrences.KillOccurrence;
import io.github.townyadvanced.flagwar.battle_tracking.model.results.BattleSnapshot;
import io.github.townyadvanced.flagwar.battle_tracking.model.results.PlayerSnapshot;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.potion.PotionType;

import java.util.*;

/** A class representing a single player's tracked encounters and actions during a BannerWar battle. */
public class TrackedPlayer {

    /**
     * Deserializes a map of {@link PlayerSnapshot}s into a map of {@link TrackedPlayer}s and returns it.
     * @param map the specified map of {@link PlayerSnapshot}s
     */
    public static Map<UUID, TrackedPlayer> fromMap(Map<String, PlayerSnapshot> map) {
        Map<UUID, TrackedPlayer> out = new HashMap<>();
        for (var item : map.values()) {
            var newTracked = new TrackedPlayer(item);
            out.put(newTracked.getOfflinePlayer().getUniqueId(), newTracked);
        }
        return out;
    }

    // INSTANCE STUFF BEGINS HERE.

    /** Holds the player that this object represents. */
    private final OfflinePlayer PLAYER;

    /** Holds the affiliation of this player in a battle. */
    private Affiliation AFFILIATION;

    /** Holds the total damage dealt by this player since the start of this battle. */
    private double damageDealt;

    /** Holds the total damage taken by this player since the start of this battle. */
    private double damageTaken;

    /** Holds a collection of all kill events this player is responsible for since the start of this battle. */
    private final Collection<KillOccurrence> KILLS;

    /** Holds a collection of all kill events this player has died in since the start of this battle. */
    private final Collection<KillOccurrence> DEATHS;

    /** Holds a collection of all flag events that they played a part in (placed and won, or destroyed). */
    private final Collection<FlagOccurrence> FLAGS;

    /** Holds a map of every {@link Material} consumed by this player, and how much of each material. */
    private final Map<Material, Integer> CONSUMED_ITEMS;

    /** Holds a map of every {@link PotionType} consumed by this player, and how much of each type. */
    private final Map<PotionType, Integer> CONSUMED_POTIONS;

    private TrackedPlayer(
    OfflinePlayer p, Affiliation affiliation, double damageDealt, double damageTaken, Collection<KillOccurrence> kills, Collection<KillOccurrence> deaths, Collection<FlagOccurrence> flags, int potsUsed, int gapsUsed) {
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

    /**
     * Returns a new {@link TrackedPlayer}.
     * @param p the {@link OfflinePlayer} representing this player for persistence beyond the server and player
     * @param affiliation the affiliation of this player
     */
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

    /**
     * Resumes the tracking of a BannerWar battle from a {@link PlayerSnapshot}.
     * @param snapshot the snapshot
     */
    public TrackedPlayer(PlayerSnapshot snapshot) {
        this(
            Bukkit.getOfflinePlayer(snapshot.playerName()),
            snapshot.affiliation(),
            snapshot.damageDealt(),
            snapshot.damageTaken(),
            snapshot.kills(),
            snapshot.deaths(),
            snapshot.flags(),
            snapshot.potsUsed(),
            snapshot.gapsUsed()
        );
    }

    /** Adds the specified {@link KillOccurrence} to the player's {@link #KILLS} collection. */
    public void incrementKills(KillOccurrence killOccurrence) {
        KILLS.add(killOccurrence);
    }

    /** Adds the specified {@link KillOccurrence} to the player's {@link #DEATHS} collection. */
    public void incrementDeaths(KillOccurrence killOccurrence) {
        DEATHS.add(killOccurrence);
    }

    /** Adds the specified damage to the total damage dealt by this player. */
    public void addDamageDealt(double damageDealt) {
        this.damageDealt += damageDealt;
    }

    /** Adds the specified damage to the total damage taken by this player. */
    public void addDamageTaken(double damageTaken) {
        this.damageTaken += damageTaken;
    }

    /** Increments the number of times the specified item is consumed by 1. */
    public void registerConsumedItem(Material item) {
        CONSUMED_ITEMS.put(item, CONSUMED_ITEMS.getOrDefault(item, 0) + 1);
    }

    /** Increments the number of times the specified potion is consumed by 1. */
    public void registerConsumedPotion(PotionType type) {
        CONSUMED_POTIONS.put(type, CONSUMED_POTIONS.getOrDefault(type, 0) + 1);
    }

    /** Adds the specified completed {@link FlagOccurrence} to the player's {@link #FLAGS} collection. */
    public void registerFlag(FlagOccurrence flagOccurrence) {
        FLAGS.add(flagOccurrence);
    }

    /** Returns the {@link OfflinePlayer} represented by this object. */
    public OfflinePlayer getOfflinePlayer() {
        return PLAYER;
    }

    /** Returns the {@link Affiliation} of this player. */
    public Affiliation getAffiliation() {
        return AFFILIATION;
    }

    /** Sets the {@link Affiliation} of this player. */
    public void setAffiliation(Affiliation affiliation) {
        this.AFFILIATION = affiliation;
    }

    /** Returns the damage dealt by this player. */
    public double getDamageDealt() {
        return damageDealt;
    }

    /** Returns the damage taken by this player. */
    public double getDamageTaken() {
        return damageTaken;
    }

    /** Returns the number of times the specified potion type has been consumed by this player. */
    public int getPotionsUsed(PotionType type) {
        return CONSUMED_POTIONS.getOrDefault(type, 0);
    }

    /** Returns the number of times the specified material has been consumed by this player. */
    public int getItemsUsed(Material type) {
        return CONSUMED_ITEMS.getOrDefault(type, 0);
    }

    /** Returns the player's {@link #KILLS} collection. */
    public Collection<KillOccurrence> getKills() {
        return KILLS;
    }

    /** Returns the player's {@link #DEATHS} collection. */
    public Collection<KillOccurrence> getDeaths() {
        return DEATHS;
    }

    /** Returns the player's {@link #FLAGS} collection. */
    public Collection<FlagOccurrence> getFlagLogs() {
        return FLAGS;
    }
}
