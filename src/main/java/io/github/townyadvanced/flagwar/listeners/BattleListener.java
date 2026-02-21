package io.github.townyadvanced.flagwar.listeners;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.actions.TownyBuildEvent;
import com.palmergames.bukkit.towny.object.*;
import io.github.townyadvanced.flagwar.BannerWarAPI;
import io.github.townyadvanced.flagwar.BattleManager;
import io.github.townyadvanced.flagwar.Broadcasts;
import io.github.townyadvanced.flagwar.config.FlagWarConfig;
import io.github.townyadvanced.flagwar.events.BattleEndEvent;
import io.github.townyadvanced.flagwar.events.BattleFlagEvent;
import io.github.townyadvanced.flagwar.events.BattleStartEvent;
import io.github.townyadvanced.flagwar.objects.Battle;
import io.github.townyadvanced.flagwar.objects.BattleStage;
import io.github.townyadvanced.flagwar.util.BannerWarUtil;
import io.github.townyadvanced.flagwar.util.FormatUtil;
import org.bukkit.ChatColor;
import org.bukkit.Tag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class BattleListener implements Listener {

    private final BattleManager BMGR;

    /** The error message for a minimum number of players. */
    private static final String PLAYERS_ONLINE_ERROR =
        ChatColor.RED + "There must be a minimum of %s online in %s for a battle to occur!";

    public BattleListener(final BattleManager bmgr) {
        BMGR = bmgr;
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onBannerBlockPlace(TownyBuildEvent event){
        TownBlock townBlock = event.getTownBlock();

        if (townBlock == null
            || townBlock.getTownOrNull() == null
            || !Tag.BANNERS.isTagged(event.getMaterial()))
                return;

        if (TownyAPI.getInstance().getResident(event.getPlayer().getUniqueId()) == null) return;

        Resident r = TownyAPI.getInstance().getResident(event.getPlayer().getUniqueId());
        Town town = townBlock.getTownOrNull();

        if (town.getNationOrNull() == null
            || r == null || r.getTownOrNull() == null
            || r.getTownOrNull().getNationOrNull() == null)
                return;

        Nation defender = town.getNationOrNull();
        Nation attacker = r.getTownOrNull().getNationOrNull();
        int minOnInTown = FlagWarConfig.getMinPlayersOnlineInTownForWar();
        int minOnInNation = FlagWarConfig.getMinPlayersOnlineInNationForWar();
        int minOnInAttackerNation = FlagWarConfig.getMinAttackingPlayersOnlineInNationForWar();
        int minOnInAttackerTown = FlagWarConfig.getMinAttackingPlayersOnlineInTownForWar();

        int onlineResidents =
            town.getResidents().stream().filter(Resident::isOnline).toList().size();

        if (attacker.hasAlly(defender) || attacker.equals(defender)) return;


        if (!townBlock.getWorld().isWarAllowed()) {
            Broadcasts.sendMessage(event.getPlayer(), ChatColor.RED + "The world is not allowed to war!");
            return;
        }

        if (!town.isAllowedToWar()) {
            Broadcasts.sendMessage(event.getPlayer(), ChatColor.RED + "The town is not allowed to war!");
            return;
        }

        if (!FlagWarConfig.isAllowingAttacks()) {
            Broadcasts.sendMessage(event.getPlayer(), ChatColor.RED + "This server is not configured to allow attacks!");
            return;
        }

        if (defender.isNeutral()) {
            Broadcasts.sendMessage(event.getPlayer(), ChatColor.RED + "You cannot attack a peaceful nation!");
            return;
        }

        if (BannerWarAPI.isInBattle(town)) {

            Battle battle = BattleManager.getBattle(town.getName());

            if (battle.getStage() ==  BattleStage.DORMANT)
                Broadcasts.sendMessage(event.getPlayer(), ChatColor.RED + "This town has recently been in a battle! " +
                    "You can attack it again in " + FormatUtil.getFormattedTime(battle.getTimeRemainingForCurrentStage()) + ".");
            else
                Broadcasts.sendMessage(event.getPlayer(), ChatColor.RED + "This town is already under a battle!");

            return;
        }

        if (!attacker.hasEnemy(defender)) {
            Broadcasts.sendMessage(event.getPlayer(), ChatColor.RED + "You are not enemies with this nation!");
            return;
        }

        if (onlineResidents < minOnInTown) {
            Broadcasts.sendMessage(event.getPlayer(), String.format(PLAYERS_ONLINE_ERROR, FormatUtil.tryPluralize("player", minOnInTown), town.getName()));
            return;
        }

        if (onlineResidents < minOnInNation) {
            Broadcasts.sendMessage(event.getPlayer(), String.format(PLAYERS_ONLINE_ERROR, FormatUtil.tryPluralize("player", minOnInNation), defender.getName()));
            return;
        }

        if (onlineResidents < minOnInAttackerNation) {
            Broadcasts.sendMessage(event.getPlayer(), String.format(PLAYERS_ONLINE_ERROR, FormatUtil.tryPluralize("player", minOnInAttackerNation), attacker));
            return;
        }

        if (onlineResidents < minOnInAttackerTown) {
            Broadcasts.sendMessage(event.getPlayer(), String.format(PLAYERS_ONLINE_ERROR, FormatUtil.tryPluralize("player", minOnInAttackerTown), r.getTownOrNull().getName()));
            return;
        }

        event.setCancelled(false);
        BMGR.startBattle(town, attacker, defender);

    }

    @EventHandler
    public void onBattleStart(BattleStartEvent event) {
        Battle battle = event.getBattle();
        Town contestedTown = battle.getContestedTown();
        Nation defender = battle.getDefender();
        Nation attacker = battle.getAttacker();

        Broadcasts.broadcastMessage( attacker.getName() + " has initiated a battle on " + defender.getName() + " at " + contestedTown.getName() + "!");
        Broadcasts.broadcastMessage( "The battle will last " + FormatUtil.getFormattedTime(BannerWarUtil.getActivePeriod(battle)) + "!");
        System.out.println("Battle started! preflag is " + battle.getDuration(BattleStage.PRE_FLAG));
        System.out.println("flag is " + battle.getDuration(BattleStage.FLAG));
        System.out.println("together is " + battle.getDuration(BattleStage.FLAG).plus(battle.getDuration(BattleStage.PRE_FLAG)));
    }

    @EventHandler
    public void onBattleEnd(BattleEndEvent event) {
        Battle battle = event.getBattle();
        String def = battle.getDefender().getName();
        String att = battle.getAttacker().getName();
        String twn = battle.getContestedTown().getName();

        String message = event.isDefenseWon() ?
            def + " has successfully defended " + att + " from " + twn + " and won the battle!" :
            att + " has successfully conquered " + twn + " from " + def + "! The attacker now has free-range over the town!";

        Broadcasts.broadcastMessage(message);
    }

    @EventHandler
    public void onBeginFlag(BattleFlagEvent event) {
        Battle battle = event.getBattle();
        Broadcasts.broadcastMessage(
            "The battle at " + battle.getContestedTown().getName() + " has begun its " + ChatColor.AQUA + "FLAG" + ChatColor.RESET + " state. Flags may now be placed!");
    }
}
