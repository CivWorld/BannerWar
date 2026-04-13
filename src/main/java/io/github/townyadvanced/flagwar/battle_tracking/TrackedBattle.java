package io.github.townyadvanced.flagwar.battle_tracking;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import io.github.townyadvanced.flagwar.BannerWarAPI;
import io.github.townyadvanced.flagwar.battle_tracking.occurrences.DamageOccurrence;
import io.github.townyadvanced.flagwar.battle_tracking.occurrences.KillOccurrence;
import io.github.townyadvanced.flagwar.battle_tracking.util.BattleRegionDeterminer;
import io.github.townyadvanced.flagwar.objects.Battle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;


public class TrackedBattle {

    private static final Map<UUID, TrackedBattle> TRACKED_BATTLES = new HashMap<>();

    public static TrackedBattle getBattleAt(Location location) {
        return getBattleAt(location.toVector());
    }

    public static TrackedBattle getBattleAt(Vector position) {
        for (var battle : TRACKED_BATTLES.values()) {
            if (battle.isInBattleRegion(position)) return battle;
        }
        return null;
    }

    public static void trackBattle(Battle battle) {
        Town town = battle.getContestedTown();
        TRACKED_BATTLES.put(town.getUUID(), new TrackedBattle(town, battle.getAttacker(), battle.getDefender()));
    }

    // INSTANCE FIELDS

    private final Town TOWN;
    private final Nation ATTACKER;
    private final Nation DEFENDER;
    private final Collection<BoundingBox> BATTLE_REGION;
    private final Map<UUID, TrackedFighter> TRACKED_PLAYERS = new HashMap<>();
    private final Deque<DamageOccurrence> DAMAGE_OCCURENCES = new ArrayDeque<>();
    private final Deque<KillOccurrence> KILL_OCCURENCES = new ArrayDeque<>();

    private TrackedBattle(Town town, Nation attacker, Nation defender) {
        this.ATTACKER = attacker;
        this.DEFENDER = defender;
        this.TOWN = town;
        this.BATTLE_REGION = BattleRegionDeterminer.determineRegionFor(town);
    }

    public boolean isInBattleRegion(Vector position) {
        for (BoundingBox b : BATTLE_REGION)
            if (b.contains(position)) return true;

        return false;
    }

    public void addKillOccurrence(Entity killer, Player killed, ItemStack weapon, EntityDamageEvent.DamageCause cause) {
        KILL_OCCURENCES.add(KillOccurrence.from(killer, killed, weapon, cause));
        getTrackedFighter(killed).incrementDeaths();

        if (killer instanceof Player killerPlayer) {
            getTrackedFighter(killerPlayer).incrementKills();
        }
    }

    public void addDamageOccurrence(Entity hurter, Entity hurted, double damage) {
        if (hurted instanceof Player hurtedPlayer) {
            DAMAGE_OCCURENCES.add(DamageOccurrence.from(hurter, hurted, damage));
            getTrackedFighter(hurtedPlayer).addDamageTaken(damage);

            if (hurter instanceof Player hurterPlayer) {
                getTrackedFighter(hurterPlayer).addDamageDealt(damage);
            }
        }
    }

    public void onConsume(Player consumer, ItemStack item) {
        if (item.getItemMeta() instanceof PotionMeta pm)
            getTrackedFighter(consumer).registerConsumedPotion(pm.getBasePotionData().getType());

        else getTrackedFighter(consumer).registerConsumedItem(item.getType());
    }

    public void onPotionThrow(@NotNull Player thrower, @NotNull ThrownPotion throwable) {
        getTrackedFighter(thrower).registerConsumedPotion(throwable.getPotionMeta().getBasePotionData().getType());
    }

    public void flagPlaceEvent(String flagOwnerName) {
        getTrackedFighter(Bukkit.getOfflinePlayer(flagOwnerName)).incrementFlagsPlaced();
    }

    public void flagSuccessEvent(String flagOwnerName) {
        getTrackedFighter(Bukkit.getOfflinePlayer(flagOwnerName)).incrementFlagsSucceeded();
    }

    public void flagBreakEvent(String flagOwnerName, Player breaker) {
        if (breaker.getName().equals(flagOwnerName)) return; // if you broke your own flag.
        Resident rBreaker = TownyAPI.getInstance().getResident(breaker);
        Resident rFlagOwner = TownyAPI.getInstance().getResident(flagOwnerName);

        if (rFlagOwner == null
            || rFlagOwner.getTownOrNull() == null
            || rFlagOwner.getTownOrNull().getNationOrNull() == null) return;

        if (BannerWarAPI.isAssociatedWithNation(rBreaker, rFlagOwner.getTownOrNull().getNationOrNull())) return;

        getTrackedFighter(breaker).incrementFlagsDestroyed();

    }

    public @NotNull TrackedFighter getTrackedFighter(OfflinePlayer p) {
        var trackedPlayer = TRACKED_PLAYERS.getOrDefault(p.getUniqueId(), null);
        if (trackedPlayer == null) {
            trackedPlayer = new TrackedFighter(p);
            TRACKED_PLAYERS.put(p.getUniqueId(), trackedPlayer);
        }

        return trackedPlayer;
    }
}
