package io.github.townyadvanced.flagwar.battle_tracking;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import io.github.townyadvanced.flagwar.BannerWarAPI;
import io.github.townyadvanced.flagwar.FlagWar;
import io.github.townyadvanced.flagwar.battle_tracking.structures.enums.Affiliation;
import io.github.townyadvanced.flagwar.battle_tracking.structures.occurrences.DamageOccurrence;
import io.github.townyadvanced.flagwar.battle_tracking.structures.occurrences.FlagOccurrence;
import io.github.townyadvanced.flagwar.battle_tracking.structures.occurrences.KillOccurrence;
import io.github.townyadvanced.flagwar.battle_tracking.structures.results.TrackedBattleResult;
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

    private final Town TOWN;
    private final Nation ATTACKER;
    private final Nation DEFENDER;
    private final long UNIX_START_TIME;
    private final Collection<BoundingBox> BATTLE_REGION;
    private final Map<UUID, TrackedPlayer> TRACKED_PLAYERS;
    private final Collection<DamageOccurrence> DAMAGE_OCCURRENCES;

    private TrackedBattle(Town town, Nation attacker, Nation defender, long startTime, Map<UUID, TrackedPlayer> trackedPlayers, Collection<DamageOccurrence> damageOccurrences) {
        System.out.println("INSTANTIATED A NEW BATTLE");
        this.TOWN = town;
        this.ATTACKER = attacker;
        this.DEFENDER = defender;
        this.UNIX_START_TIME = startTime;
        BATTLE_REGION = BattleRegionDeterminer.determineRegionFor(town);
        TRACKED_PLAYERS = trackedPlayers;
        DAMAGE_OCCURRENCES = damageOccurrences;
    }

    TrackedBattle(Town town, Nation attacker, Nation defender) {
        this(
            town,
            attacker,
            defender,
            System.currentTimeMillis(),
            new HashMap<>(),
            new ArrayDeque<>()
        );
    }

    TrackedBattle(TrackedBattleResult trackedBattleResult) {
        this(
            TownyAPI.getInstance().getTown(trackedBattleResult.townName()),
            TownyAPI.getInstance().getNation(trackedBattleResult.attackerNationName()),
            TownyAPI.getInstance().getNation(trackedBattleResult.defenderNationName()),
            trackedBattleResult.unixStartTime(),
            TrackedPlayer.fromMap(trackedBattleResult.playerResultMap()),
            trackedBattleResult.damageOccurrences()
        );
        System.out.println("INSTANTIATED A NEW BATTLE");
    }

    boolean isInBattleRegion(Vector position) {
        for (BoundingBox b : BATTLE_REGION)
            if (b.contains(position)) return true;
        return false;
    }

    public void addKillOccurrence(Entity killer, Player killed, ItemStack weapon, EntityDamageEvent.DamageCause cause) {
        var killOccurrence = KillOccurrence.from(killer, killed, weapon, cause);
        getTrackedPlayer(killed).incrementDeaths(killOccurrence);

        if (killer instanceof OfflinePlayer killerPlayer) {
            getTrackedPlayer(killerPlayer).incrementKills(killOccurrence);
        }
    }

    public void addDamageOccurrence(Entity hurter, Entity hurted, double damage) {
        if (hurted instanceof Player hurtedPlayer) {
            DAMAGE_OCCURRENCES.add(DamageOccurrence.from(hurter, hurted, damage));
            getTrackedPlayer(hurtedPlayer).addDamageTaken(damage);

            if (hurter instanceof Player hurterPlayer) {
                getTrackedPlayer(hurterPlayer).addDamageDealt(damage);
            }
        }
    }

    public void onConsume(Player consumer, ItemStack item) {
        if (item.getItemMeta() instanceof PotionMeta pm)
            getTrackedPlayer(consumer).registerConsumedPotion(pm.getBasePotionData().getType());

        else getTrackedPlayer(consumer).registerConsumedItem(item.getType());
    }

    public void onPotionThrow(@NotNull Player thrower, @NotNull ThrownPotion throwable) {
        getTrackedPlayer(thrower).registerConsumedPotion(throwable.getPotionMeta().getBasePotionData().getType());
    }


    public void flagSuccessEvent(FlagOccurrence flagOccurrence) {
        registerFlag(flagOccurrence);
    }

    public void flagBreakEvent(FlagOccurrence flagOccurrence) {
        String flagPlacer = flagOccurrence.flagPlacer();
        String flagDestroyer = flagOccurrence.flagDestroyer();

        if (flagPlacer.equals(flagDestroyer)) return; // if you broke your own flag.
        Resident rBreaker = TownyAPI.getInstance().getResident(flagDestroyer);
        Resident rFlagOwner = TownyAPI.getInstance().getResident(flagPlacer);

        if (rFlagOwner == null
            || rFlagOwner.getTownOrNull() == null
            || rFlagOwner.getTownOrNull().getNationOrNull() == null) return;

        // if you broke your ally's flag.
        if (BannerWarAPI.isAssociatedWithNation(rBreaker, rFlagOwner.getTownOrNull().getNationOrNull())) return;

        getTrackedPlayer(flagPlacer).registerFlag(flagOccurrence);
        getTrackedPlayer(flagDestroyer).registerFlag(flagOccurrence);
    }

    public void flagCancelEvent(FlagOccurrence flagOccurrence) {
        registerFlag(flagOccurrence);
    }

    public void registerFlag(FlagOccurrence flagOccurrence) {
        getTrackedPlayer(Bukkit.getOfflinePlayer(flagOccurrence.flagPlacer())).registerFlag(flagOccurrence);
    }

    public @NotNull TrackedPlayer getTrackedPlayer(OfflinePlayer p) {
        System.out.println("Queried tracked player " + p.getName());
        return TRACKED_PLAYERS.computeIfAbsent(p.getUniqueId(),
            id -> new TrackedPlayer(p, determineAffiliation(id)));
    }

    public @NotNull TrackedPlayer getTrackedPlayer(String name) {
        return getTrackedPlayer(Bukkit.getOfflinePlayer(name));
    }

    private Affiliation determineAffiliation(UUID id) {
        Resident resident = TownyAPI.getInstance().getResident(id);

        if (BannerWarAPI.isAssociatedWithNation(resident, ATTACKER)) return Affiliation.ATTACKER;
        if (BannerWarAPI.isAssociatedWithNation(resident, DEFENDER)) return Affiliation.DEFENDER;
        return Affiliation.VAGRANT;
    }

    public Collection<TrackedPlayer> getTrackedPlayers() {
        return TRACKED_PLAYERS.values();
    }

    public long getStartTime() {
        return UNIX_START_TIME;
    }

    public Town getTown() {
        return TOWN;
    }

    public Nation getAttacker() {
        return ATTACKER;
    }

    public Nation getDefender() {
        return DEFENDER;
    }

    public Collection<DamageOccurrence> getDamageOccurrences() {
        return DAMAGE_OCCURRENCES;
    }
}
