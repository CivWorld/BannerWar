package io.github.townyadvanced.flagwar.command;

import io.github.townyadvanced.flagwar.BannerWarAPI;
import io.github.townyadvanced.flagwar.Broadcasts;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class StageAdvance implements CommandExecutor {

    private final JavaPlugin PLUGIN;

    public StageAdvance(JavaPlugin plugin) {
        this.PLUGIN = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        boolean toWin;

        if (strings.length == 1) toWin = true;
        else if (strings.length == 2) toWin = Boolean.parseBoolean(strings[1]);
        else return false;

        String townName = strings[0];

        if (commandSender instanceof Player p) {
            if (!p.isOp()) {
                Broadcasts.sendMessage(p, ChatColor.RED + "You do not have permission to execute this command.");
                return true;
            }

            else if (BannerWarAPI.getBattle(townName) != null) {
                BannerWarAPI.getBattle(townName).advanceStage(toWin);
                Broadcasts.sendMessage(p, ChatColor.GREEN + "Advanced stage for " + townName + "!");
            }
            else {
                Broadcasts.sendMessage(p, ChatColor.RED + townName + " is not in a battle or does not exist!");
                return true;
            }
        }

        else {
            if (BannerWarAPI.getBattle(townName) != null) {
                BannerWarAPI.getBattle(townName).advanceStage(toWin);
                PLUGIN.getLogger().info("Advanced stage for " + townName + "!");
            }
            else {
                PLUGIN.getLogger().warning(townName + " is not in a battle or does not exist!");
                return true;
            }
        }

        return true;
    }
}
