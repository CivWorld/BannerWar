package io.github.townyadvanced.flagwar.battle_tracking;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.potion.PotionType;

import java.util.HashMap;
import java.util.Map;

public class TrackedFighter {
    private final OfflinePlayer PLAYER;

    private double damageDealt = 0.0;
    private double damageTaken = 0.0;

    private int kills = 0;
    private int deaths = 0;

    private int flagsPlaced = 0;
    private int flagsSucceeded = 0;
    private int flagsDestroyed = 0;

    private final Map<Material, Integer> CONSUMED_ITEMS = new HashMap<>();
    private final Map<PotionType, Integer> CONSUMED_POTIONS =  new HashMap<>();

    public TrackedFighter(OfflinePlayer p) {
        this.PLAYER = p;
    }

    public void incrementKills() {
        kills++;
    }

    public void incrementDeaths() {
        deaths++;
    }

    public void addDamageDealt(double damageDealt) {
        this.damageDealt += damageDealt;
    }
    public void addDamageTaken(double damageTaken) {
        this.damageTaken += damageTaken;
    }

    public void incrementFlagsPlaced() {
        flagsPlaced++;
    }

    public void incrementFlagsSucceeded() {
        flagsSucceeded++;
    }

    public void incrementFlagsDestroyed() {
        flagsDestroyed++;
    }

    public void registerConsumedItem(Material item) {
        CONSUMED_ITEMS.put(item, CONSUMED_ITEMS.getOrDefault(item, 0) + 1);
    }

    public void registerConsumedPotion(PotionType type) {
        CONSUMED_POTIONS.put(type, CONSUMED_POTIONS.getOrDefault(type, 0) + 1);
    }
}
