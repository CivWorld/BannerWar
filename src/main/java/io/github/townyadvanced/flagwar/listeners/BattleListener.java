package io.github.townyadvanced.flagwar.listeners;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.actions.TownyBuildEvent;
import com.palmergames.bukkit.towny.object.*;
import io.github.townyadvanced.flagwar.Broadcasts;
import io.github.townyadvanced.flagwar.FlagWar;
import io.github.townyadvanced.flagwar.config.FlagWarConfig;
import org.bukkit.ChatColor;
import org.bukkit.Tag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class BattleListener implements Listener {

    private Towny TOWNY;

    public BattleListener(final FlagWar flagWar) {
        if (flagWar.getServer().getPluginManager().getPlugin("Towny") != null) {
            this.TOWNY = Towny.getPlugin();
        }
    }

    @EventHandler
    public void onBannerBlockPlace(TownyBuildEvent event){
        TownBlock townBlock = event.getTownBlock();

        if (townBlock == null) return;
        if (townBlock.getTownOrNull() == null) return;
        if (!Tag.BANNERS.isTagged(event.getMaterial())) return;
        if (TownyAPI.getInstance().getResident(event.getPlayer().getUniqueId()) == null) return;
        Resident r = TownyAPI.getInstance().getResident(event.getPlayer().getUniqueId());
        Town town = townBlock.getTownOrNull();
        if (town.getNationOrNull() == null) return;
        if (r == null || r.getTownOrNull() == null || r.getTownOrNull().getNationOrNull() == null) return;

        Nation defender = town.getNationOrNull();
        Nation attacker = r.getTownOrNull().getNationOrNull();

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

        if (!attacker.hasEnemy(defender)) {
            Broadcasts.sendMessage(event.getPlayer(), ChatColor.RED + "You are not enemies with this nation!");
            return;
        }

        event.setCancelled(true);
        event.getPlayer().sendMessage("okay assume a battle starts now.");
    }
}
