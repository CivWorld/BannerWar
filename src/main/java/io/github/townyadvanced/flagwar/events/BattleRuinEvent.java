package io.github.townyadvanced.flagwar.events;

import io.github.townyadvanced.flagwar.objects.Battle;
import io.github.townyadvanced.flagwar.objects.BattleStage;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

/** An event fired when a battle has its contested town be ruined. */
public class BattleRuinEvent extends Event {
    private static final HandlerList h = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {return h;}
    public static HandlerList getHandlerList() {
        return h;
    }

    private final Battle battle;

    public BattleRuinEvent(Battle battle) {
        this.battle = battle;
    }

    public Battle getBattle() {
        return battle;
    }

    /** Returns the {@link Duration} that this {@link Battle#getContestedTown()} will be ruined for. */
    public Duration getRuinDuration() {
        return battle.getDuration(BattleStage.RUINED);
    }
}
