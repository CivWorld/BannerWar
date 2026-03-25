package io.github.townyadvanced.flagwar.command;

import io.github.townyadvanced.flagwar.util.Broadcasts;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class ReloadConfig implements CommandExecutor {

    /** Holds the {@link Plugin} instance. */
    private final Plugin plugin;

    public ReloadConfig(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {

        if (commandSender instanceof Player p) {
            if (!p.isOp())
                Broadcasts.sendErrorMessage(p, "You do not have permission to execute this command.");
            else {
                try {
                    plugin.reloadConfig();
                    Broadcasts.sendMessage(p, "Config reload successful!", ChatColor.GREEN);
                } catch (Exception e) {
                    Broadcasts.sendErrorMessage(p, "Failed to reload config! " + e.getMessage());
                }
            }
        } else if (commandSender instanceof ConsoleCommandSender) {
            try {
                plugin.reloadConfig();
                plugin.getLogger().info("Config reload successful!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}
