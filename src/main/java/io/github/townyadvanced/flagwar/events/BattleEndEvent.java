package io.github.townyadvanced.flagwar.events;

import com.palmergames.bukkit.towny.object.Nation;
import io.github.townyadvanced.flagwar.objects.Battle;
import org.bukkit.ChatColor;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;


/** An event fired when a battle exceeds its FLAG period and ends. */
public class BattleEndEvent extends Event {
    private static final HandlerList h = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {return h;}
    public static HandlerList getHandlerList() {
        return h;
    }

    private final Battle battle;
    private final boolean isWon;

    public BattleEndEvent(Battle battle, boolean won) {
        this.battle = battle;
        this.isWon = won;
    }

    public Battle getBattle() {
        return battle;
    }

    /** Returns whether the {@link Battle}'s defending {@link Nation} won the battle. */
    public boolean isDefenseWon() { return isWon; }
}
