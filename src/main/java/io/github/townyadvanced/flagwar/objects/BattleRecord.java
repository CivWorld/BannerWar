package io.github.townyadvanced.flagwar.objects;

import com.palmergames.bukkit.towny.object.TownBlock;

import java.util.Collection;
import java.util.UUID;

/**
 * A record of a battle that can be stored in persistent storage.
 * @param contestedTown the contested town's name
 * @param attacker the attacking nation's name
 * @param defender the defending nation's name
 * @param homeX the X coordinate of the homeblock
 * @param homeZ the Z coordinate of the homeblock
 * @param stageStartTime the time, in milliseconds, where the current stage started
 * @param isCityState whether the town this battle is hosted in is a CityState
 * @param stage the {@link BattleStage} that this battle is currently on
 * @param worldID the {@link UUID} of the world that this battle is hosted in
 * @param townBlocks the {@link Collection} of {@link TownBlock}s that the contested town accommodated before the battle began
 * @param initialMayorID the {@link UUID} of the resident who was mayor before the battle began
 */
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
    Collection<TownBlock> townBlocks,
    UUID initialMayorID
)
{
    public static BattleRecord of(Battle b) {
        return new BattleRecord(
            b.getContestedTown() == null ? "_" : b.getContestedTown().getName(),
            b.getAttacker() == null ? "_" : b.getAttacker().getName(),
            b.getDefender() == null ? "_" : b.getDefender().getName(),
            b.getHomeBlock().getX(),
            b.getHomeBlock().getZ(),
            b.getStageStartTime(),
            b.isCityState(),
            b.getCurrentStage(),
            b.getContestedTown().getWorld().getUID(),
            b.getInitialTownBlocks(),
            b.getInitialMayor().getUUID()
        );
    }
}
