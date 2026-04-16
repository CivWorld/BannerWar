package io.github.townyadvanced.flagwar.battle_tracking;

import io.github.townyadvanced.flagwar.battle_tracking.structures.enums.Affiliation;
import io.github.townyadvanced.flagwar.battle_tracking.structures.occurrences.FlagOccurrence;
import io.github.townyadvanced.flagwar.battle_tracking.structures.occurrences.KillOccurrence;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.potion.PotionType;

import java.util.*;

public class TrackedPlayer {
    private final OfflinePlayer PLAYER;
    private final Affiliation AFFILIATION; // the issue of players switching teams mid-battle comes up
    // recalculate affiliation at the end of the battle?

    private double damageDealt = 0.0;
    private double damageTaken = 0.0;

    private final Deque<KillOccurrence> KILLS = new ArrayDeque<>();
    private final Deque<KillOccurrence> DEATHS = new ArrayDeque<>();
    private final Deque<FlagOccurrence> FLAGS_PLACED = new ArrayDeque<>();

    private final Map<Material, Integer> CONSUMED_ITEMS = new EnumMap<>(Material.class);
    private final Map<PotionType, Integer> CONSUMED_POTIONS = new EnumMap<>(PotionType.class);

    public TrackedPlayer(OfflinePlayer p, Affiliation affiliation) {
        this.PLAYER = p;
        this.AFFILIATION = affiliation;
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
        FLAGS_PLACED.add(flagOccurrence);
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
        return FLAGS_PLACED;
    }
}
