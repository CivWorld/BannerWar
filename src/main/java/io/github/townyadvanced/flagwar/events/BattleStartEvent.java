package io.github.townyadvanced.flagwar.events;

import io.github.townyadvanced.flagwar.objects.Battle;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/** An event fired when a battle is initiated. */
public class BattleStartEvent extends Event {
    private static final HandlerList h = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {return h;}
    public static HandlerList getHandlerList() {
        return h;
    }

    private final Battle battle;

    public BattleStartEvent(Battle battle) {
        this.battle = battle;
    }

    public Battle getBattle() {
        return battle;
    }

}
