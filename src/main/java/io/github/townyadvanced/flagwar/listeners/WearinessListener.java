package io.github.townyadvanced.flagwar.listeners;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.event.NewDayEvent;
import com.palmergames.bukkit.towny.event.town.TownLeaveEvent;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.WorldCoord;
import io.github.townyadvanced.flagwar.BannerWarAPI;
import io.github.townyadvanced.flagwar.BattleManager;
import io.github.townyadvanced.flagwar.Civics;
import io.github.townyadvanced.flagwar.config.BannerWarConfig;
import io.github.townyadvanced.flagwar.events.BattleEndEvent;
import io.github.townyadvanced.flagwar.events.CellAttackEvent;
import io.github.townyadvanced.flagwar.events.CellDefendedEvent;
import io.github.townyadvanced.flagwar.events.CellWonEvent;
import io.github.townyadvanced.flagwar.objects.Battle;
import io.github.townyadvanced.flagwar.util.Broadcasts;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;

public class WearinessListener implements Listener {

    /** Holds the {@link JavaPlugin} instance. */
    private final JavaPlugin PLUGIN;

    /** Holds the {@link BattleManager} instance. */
    private final BattleManager BATTLE_MANAGER;

    public WearinessListener(BattleManager battleManager, JavaPlugin plugin) {
        this.PLUGIN = plugin;
        this.BATTLE_MANAGER = battleManager;
    }

    @EventHandler
    public void onCellAttack(CellAttackEvent e) {
        Battle battle = BannerWarAPI.getBattle(TownyAPI.getInstance().getTownBlock(e.getFlagBlock().getLocation()));
        if (battle == null) {
            PLUGIN.getLogger().warning("The flag placed by " + e.getData().getNameOfFlagOwner() + " is flagging during a null battle!");
            return;
        }

        Resident r = TownyAPI.getInstance().getResident(e.getPlayer());

        Civics.increaseWeariness(r, 1); // TODO MAKE CONFIGURABLE

    }

    @EventHandler
    public void onCellDefend(CellDefendedEvent e) {

        WorldCoord coord = new WorldCoord(e.getPlayer().getWorld(), e.getCell().getX(), e.getCell().getZ());

        Battle battle = BannerWarAPI.getBattle(TownyAPI.getInstance().getTownBlock(coord));
        if (battle == null) {
            PLUGIN.getLogger().warning("The flag defended by " + e.getPlayer().getName() + " is during a null battle!");
            return;
        }

        Resident r = TownyAPI.getInstance().getResident(e.getCell().getAttackData().getNameOfFlagOwner());

        Civics.increaseWeariness(r, 1); // TODO MAKE CONFIGURABLE

        if (Civics.isFederation(battle.getDefender()))
            Civics.decreaseWeariness(battle.getContestedTown(), 1);
        else
            Civics.decreaseWeariness(battle.getDefender(), 1);
    }


    @EventHandler
    public void onCellWon(CellWonEvent e) {

        Battle battle = BannerWarAPI.getBattle(TownyAPI.getInstance().getTownBlock(
            e.getCellUnderAttack().getFlagBaseBlock().getLocation())
        );

        String flagOwner = e.getCellUnderAttack().getNameOfFlagOwner();

        if (battle == null) {
            PLUGIN.getLogger().warning("The flag won by " + flagOwner + " is during a null battle!");
            return;
        }

        Resident r = TownyAPI.getInstance().getResident(flagOwner);

        Civics.increaseWeariness(r, 1); // TODO MAKE CONFIGURABLE

    }

    @EventHandler
    public void onBattleEnd(BattleEndEvent e) {
        Battle battle = e.getBattle();
        // TODO MAKE ALL THIS CONFIGURABLE

        var att = battle.getAttacker();
        var def = battle.getDefender();

        if (e.isDefenseWon()) {
            Civics.increaseWeariness(att, Civics.isAutocracy(att) ? 50 : 10);
            Civics.decreaseWeariness(def, Civics.isAutocracy(def) ? 25 : 10);
        }
        else {
            if  (Civics.isAutocracy(att)) Civics.decreaseWeariness(att, 10);
            Civics.increaseWeariness(def, Civics.isAutocracy(def) ? 50 : 10);
        }
    }

    @EventHandler
    public void onTownLeave(TownLeaveEvent e) {

        // TODO im not repeating myself
        Town t = e.getTown();
        Nation n = t.getNationOrNull();

        if (Civics.isFederation(n)) {
            if (Civics.getWearinessAsPercentage(t) >= 15) {
                e.setCancelMessage(Broadcasts.prepareErrorMessage("You cannot leave this town as its war weariness exceeds 15!"));
                e.setCancelled(true);
            }
        }
        else if (Civics.getWearinessAsPercentage(n) >= 15) {
            e.setCancelMessage(Broadcasts.prepareErrorMessage("You cannot leave this town as its nation's war weariness exceeds 15!"));
            e.setCancelled(true);

        }
    }


    @EventHandler
    public void onNewTownyDay(NewDayEvent e) {

        BannerWarConfig.incrementTownyDay();

        // guaranteed weariness decrease.
        Collection<Nation> allNations = TownyUniverse.getInstance().getNations();
        for (Nation n : allNations) {
            if (Civics.isFederation(n)) Civics.decreaseWeariness(n, 2);

            else for (var town : n.getTowns())
                    Civics.decreaseWeariness(town, 2);
        }


        BATTLE_MANAGER.getAllExpiredBannerPlacers(7).thenAccept(bannerPlacers ->
            bannerPlacers.forEach(placer -> {
                if (placer.getNationOrNull() != null
                    && Civics.isFederation(placer.getNationOrNull()))
                        Civics.decreaseWeariness(placer.getNationOrNull(), 3); // TODO make configurable, original number was 5% so i just stacked 3% and the guaranteed 2%.

                else Civics.decreaseWeariness(placer, 3);
            })
        );
    }
}
