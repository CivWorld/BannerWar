package io.github.townyadvanced.flagwar.command;

import io.github.townyadvanced.flagwar.BattleManager;
import io.github.townyadvanced.flagwar.util.FormatUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class todelete implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (sender instanceof Player p)
            BattleManager.getActiveBattles().forEach(battle -> p.sendMessage(FormatUtil.getFormattedTime(battle.getTimeRemainingForCurrentStage())));
        return true;
    }
}
