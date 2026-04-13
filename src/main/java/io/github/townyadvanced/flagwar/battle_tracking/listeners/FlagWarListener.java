package io.github.townyadvanced.flagwar.battle_tracking.listeners;

import io.github.townyadvanced.flagwar.battle_tracking.TrackedBattle;
import io.github.townyadvanced.flagwar.events.*;
import io.github.townyadvanced.flagwar.objects.Cell;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

/** Listens for FlagWar and BannerWar related events, in order to know when to stop tracking battles or track flags. */
public class FlagWarListener implements Listener {

    @EventHandler (priority = EventPriority.MONITOR)
    public void onBattleStart(BattleStartEvent e) {
        TrackedBattle.trackBattle(e.getBattle());
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onBattleEnd(BattleEndEvent e) {
        // todo: add procedure for stopping battle tracking and starting uploads.
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onFlagPlace(CellAttackEvent e) {
        TrackedBattle battle = TrackedBattle.getBattleAt(e.getFlagBlock().getLocation());
        if (battle != null) battle.flagPlaceEvent(e.getPlayer().getName());
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onFlagWon(CellWonEvent e) {
        var cell = e.getCellUnderAttack();
        TrackedBattle battle = TrackedBattle.getBattleAt(cell.getFlagBaseBlock().getLocation());
        if (battle != null) battle.flagSuccessEvent(cell.getNameOfFlagOwner());
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onFlagDefend(CellDefendedEvent e) {
        Player player = e.getPlayer();
        Cell cell = e.getCell();
        Vector vec = new Vector(cell.getX()*16d, 0, cell.getZ()*16d);

        TrackedBattle battle = TrackedBattle.getBattleAt(vec);
        if (battle != null) battle.flagBreakEvent(cell.getAttackData().getNameOfFlagOwner(), player);
        Bukkit.broadcastMessage(player.getName() + " just broke the flag of " + cell.getAttackData().getNameOfFlagOwner());
    }
}
