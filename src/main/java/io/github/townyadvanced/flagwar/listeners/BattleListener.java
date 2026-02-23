package io.github.townyadvanced.flagwar.listeners;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.DeleteNationEvent;
import com.palmergames.bukkit.towny.event.DeleteTownEvent;
import com.palmergames.bukkit.towny.event.TownPreClaimEvent;
import com.palmergames.bukkit.towny.event.actions.TownyBuildEvent;
import com.palmergames.bukkit.towny.object.*;
import io.github.townyadvanced.flagwar.BannerWarAPI;
import io.github.townyadvanced.flagwar.BattleManager;
import io.github.townyadvanced.flagwar.objects.CellUnderAttack;
import io.github.townyadvanced.flagwar.util.Broadcasts;
import io.github.townyadvanced.flagwar.config.FlagWarConfig;
import io.github.townyadvanced.flagwar.events.*;
import io.github.townyadvanced.flagwar.objects.Battle;
import io.github.townyadvanced.flagwar.objects.BattleStage;
import io.github.townyadvanced.flagwar.util.BattleUtil;
import io.github.townyadvanced.flagwar.util.FormatUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class BattleListener implements Listener {

    private final JavaPlugin PLUGIN;

    /** The error message for a minimum number of players. */
    private static final String PLAYERS_ONLINE_ERROR =
        "There must be a minimum of %s online in %s for a battle to occur!";

    public BattleListener(final JavaPlugin plugin) {
        this.PLUGIN = plugin;
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onBannerBlockPlace(TownyBuildEvent event){
        TownBlock townBlock = event.getTownBlock();

        if (townBlock == null
            || townBlock.getTownOrNull() == null
            || !Tag.BANNERS.isTagged(event.getMaterial()))
                return;

        Resident r = TownyAPI.getInstance().getResident(event.getPlayer().getUniqueId());
        Town town = townBlock.getTownOrNull();

        if (r == null || r.getTownOrNull() == null
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

        Battle battle = BannerWarAPI.getBattle(townBlock);

        if (!townBlock.getWorld().isWarAllowed()) {
            Broadcasts.sendMessage(event.getPlayer(), "The world is not allowed to war!");
            return;
        }

        if (!town.isAllowedToWar()) {
            Broadcasts.sendErrorMessage(event.getPlayer(),  "The town is not allowed to war!");
            return;
        }

        if (!FlagWarConfig.isAllowingAttacks()) {
            Broadcasts.sendErrorMessage(event.getPlayer(), "This server is not configured to allow attacks!");
            return;
        }

        if (defender == null && battle == null) {
            Broadcasts.sendErrorMessage(event.getPlayer(), "This town is not part of a nation!");
            return;
        }


        if (defender != null && defender.isNeutral()) {
            Broadcasts.sendErrorMessage(event.getPlayer(), "You cannot attack a peaceful nation!");
            return;
        }

        if (battle != null) {

            if (battle.getCurrentStage() == BattleStage.DORMANT)
                Broadcasts.sendErrorMessage(event.getPlayer(),  "This town has recently been in a battle! " +
                    "You can attack it again in " + FormatUtil.getFormattedTime(battle.getTimeRemainingForCurrentStage()) + ".");
            else
                Broadcasts.sendErrorMessage(event.getPlayer(),  "This town is already under a battle!");

            return;
        }

        if (!attacker.hasEnemy(defender)) {
            Broadcasts.sendErrorMessage(event.getPlayer(), "You are not enemies with this nation!");
            return;
        }

        if (onlineResidents < minOnInTown) {
            Broadcasts.sendErrorMessage(event.getPlayer(), String.format(PLAYERS_ONLINE_ERROR, FormatUtil.tryGetPlural("player", minOnInTown), town.getName()));
            return;
        }

        if (onlineResidents < minOnInNation) {
            Broadcasts.sendErrorMessage(event.getPlayer(), String.format(PLAYERS_ONLINE_ERROR, FormatUtil.tryGetPlural("player", minOnInNation), defender.getName()));
            return;
        }

        if (onlineResidents < minOnInAttackerNation) {
            Broadcasts.sendErrorMessage(event.getPlayer(), String.format(PLAYERS_ONLINE_ERROR, FormatUtil.tryGetPlural("player", minOnInAttackerNation), attacker));
            return;
        }

        if (onlineResidents < minOnInAttackerTown) {
            Broadcasts.sendErrorMessage(event.getPlayer(), String.format(PLAYERS_ONLINE_ERROR, FormatUtil.tryGetPlural("player", minOnInAttackerTown), r.getTownOrNull().getName()));
            return;
        }

        if (!town.hasHomeBlock()) {
            Broadcasts.sendErrorMessage(event.getPlayer(), "This town does not contain a home block!");
            return;
        }

        event.setCancelled(false);
        BattleManager.startBattle(town, attacker, defender);
    }

    @EventHandler
    public void onBattleStart(BattleStartEvent event) {
        Battle battle = event.getBattle();
        Town contestedTown = battle.getContestedTown();
        Nation defender = battle.getDefender();
        Nation attacker = battle.getAttacker();

        Broadcasts.broadcastMessage( attacker.getName() + " has initiated a battle on " + defender.getName() + " at " + contestedTown.getName() + "!");
        Broadcasts.broadcastMessage( "The battle will last " + FormatUtil.getFormattedTime(BattleUtil.getActivePeriod(battle)) + "!");
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
            def + " has successfully defended " + twn + " from " + att + ". The defender is victorious!" :
            att + " has defeated " + def + " in battle, and reduced " + twn + " to ruins! The attacker is victorious!";

        Broadcasts.broadcastMessage(message);
    }

    @EventHandler
    public void onFlaggable(BattleFlaggableEvent event) {
        Battle battle = event.getBattle();
        System.out.println("time to begin flag!");
        Broadcasts.broadcastMessage(
            "The battle at " + battle.getContestedTown().getName() + " has begun its " + ChatColor.AQUA + "FLAG" + ChatColor.RESET + " state. Flags may now be placed!");
    }

    @EventHandler
    public void onTownDisband(DeleteTownEvent e) {
        for (var b : BattleManager.getActiveBattles()) {
            if (!b.isActive()) return;
            if (b.getContestedTown().getName().equals(e.getTownName())) b.loseDefense();
        }
    }

    @EventHandler
    public void onNationDisband(DeleteNationEvent e) {
        for (var b : BattleManager.getActiveBattles()) {
            if (!b.isActive()) return;
            if (b.getAttacker().getName().equals(e.getNationName())) b.winDefense();
            if (b.getDefender().getName().equals(e.getNationName())) b.loseDefense();
        }
    }

    @EventHandler
    public void onTownBlockClaim(TownPreClaimEvent e) {
        if (BannerWarAPI.isInBattle(e.getTown()) && BannerWarAPI.isNotDormant(e.getTown())) {
            e.setCancelled(true);
            e.setCancelMessage(Broadcasts.prepareErrorMessage("You cannot claim town blocks while under battle!"));
        }
    }

    @EventHandler
    public void onFlagStart(CellAttackEvent e) {
        Battle battle = BannerWarAPI.getBattle(TownyAPI.getInstance().getTownBlock(e.getFlagBlock().getLocation()));
        if (battle == null) {
            PLUGIN.getLogger().warning("The flag placed by " + e.getData().getNameOfFlagOwner() + " is flagging during a null battle!");
            return;
        }
        battle.addFlag(e.getData().getNameOfFlagOwner());
    }

    @EventHandler
    public void onFlagAttackWon(CellWonEvent e) {
        var c = e.getCellUnderAttack();
        TownBlock tb = TownyAPI.getInstance().getTownBlock(new WorldCoord(c.getWorldName(), c.getX(), c.getZ()));

        Battle battle = BannerWarAPI.getBattle(tb);
        if (battle == null) {
            PLUGIN.getLogger().warning("The flag placed by " + e.getCellUnderAttack().getNameOfFlagOwner() + " is flagging during a null battle!");
            return;
        }

        if (battle.getContestedTown().isHomeBlock(tb)) {
            e.setCancelled(true);
            battle.loseDefense();
        }
        else battle.removeFlag(c.getNameOfFlagOwner());
    }

    @EventHandler
    public void onFlagAttackLost(CellDefendedEvent e) {
        var c = e.getCell();
        TownBlock tb = TownyAPI.getInstance().getTownBlock(new WorldCoord(c.getWorldName(), c.getX(), c.getZ()));

        Battle battle = BannerWarAPI.getBattle(tb);
        if (battle == null) {
            PLUGIN.getLogger().warning("The flag" + c.getX() + "-" + c.getZ() + " is flagging during a null battle!");
            return;
        }

        battle.removeFlag(c.getAttackData().getNameOfFlagOwner());
    }

    @EventHandler
    public void onAddFlagLife(PlayerInteractEvent e) {

        Block b = e.getClickedBlock();
        Resident adder = TownyAPI.getInstance().getResident(e.getPlayer());

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getHand() == EquipmentSlot.HAND && b != null) {

            TownBlock tb = TownyAPI.getInstance().getTownBlock(b.getLocation());
            Battle battle = BannerWarAPI.getBattle(tb);

            if (adder != null && tb != null && battle != null) {
                CellUnderAttack cell = battle.getCellUnderAttack(tb.getX(), tb.getZ());

                if (cell != null) {
                    Resident placer = TownyAPI.getInstance().getResident(cell.getNameOfFlagOwner());

                    if (adder.isAlliedWith(placer) && !adder.getNationRanks().isEmpty()) {
                        ItemStack held = e.getPlayer().getInventory().getItemInMainHand();

                        if (held.getType() == Material.GOLD_INGOT) {

                            if (cell.tryAddLife())
                                Broadcasts.sendMessage(e.getPlayer(), ChatColor.GREEN + "You have added a life!");
                            else {
                                Broadcasts.sendErrorMessage(e.getPlayer(), "You cannot add any more lives!");
                                return;
                            }

                            held.setAmount(held.getAmount() - 1);
                            e.getPlayer().getInventory().setItemInMainHand(held);
                        }
                    }
                }
            }
        }
    }
}
