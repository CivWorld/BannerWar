package io.github.townyadvanced.flagwar.objects;

import com.palmergames.bukkit.towny.object.TownBlock;

import java.util.Collection;
import java.util.UUID;

public record BattleRecord (
    String contestedTown,
    String attacker,
    String defender,
    int homeX,
    int homeZ,
    long stageStartTime,
    boolean isCityState,
    BattleStage stage,
    UUID worldID,
    Collection<TownBlock> townBlocks
)
{
    public static BattleRecord of(Battle b) {
        return new BattleRecord(
            b.getContestedTown().getName(),
            b.getAttacker().getName(),
            b.getDefender().getName(),
            b.getHomeBlock().getX(),
            b.getHomeBlock().getZ(),
            b.getStageStartTime(),
            b.isCityState(),
            b.getStage(),
            b.getContestedTown().getWorld().getUID(),
            b.getInitialTownBlocks()
        );
    }
}
