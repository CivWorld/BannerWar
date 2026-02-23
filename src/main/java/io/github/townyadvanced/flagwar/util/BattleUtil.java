package io.github.townyadvanced.flagwar.util;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import io.github.townyadvanced.flagwar.objects.Battle;
import io.github.townyadvanced.flagwar.objects.BattleStage;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.time.Duration;
import java.util.*;

public final class BattleUtil {

    private BattleUtil() {}

    /** Holds the delimiter used for splitting {@link String}s. */
    private static final String DELIMITER = ":";

    /** Holds the delimiter used for splitting {@link String}s that hold chunk coordinates. */
    private static final String CHUNK_DELIMITER = "-";

    /**
     * Returns a {@link Collection} of {@link TownBlock}s from a {@link String} representation.
     * @param worldID the {@link UUID} of the world where the town blocks reside
     * @param listToSplit the {@link String} that will be split into a list.
     */
    public static Collection<TownBlock> toBlockList(String worldID, String listToSplit) {
        World world = Bukkit.getServer().getWorld(UUID.fromString(worldID));
        if (world == null) return new ArrayList<>();

        List<TownBlock> blocks = new ArrayList<>();
        String[] chunks = listToSplit.split(DELIMITER);

        for (String chunk : chunks) {
            String[] block = chunk.split(CHUNK_DELIMITER);
            int x = Integer.parseInt(block[0]);
            int z = Integer.parseInt(block[1]);


            blocks.add(TownyAPI.getInstance().getTownBlock(
                new WorldCoord(world, x, z)
            ));
        }
        return blocks;
    }

    /**
     * Returns a {@link String} representation of a {@link Collection} of {@link TownBlock}s
     * @param blocks the {@link Collection} of {@link TownBlock}s
     */
    public static String fromBlockList(Collection<TownBlock> blocks) {

        Collection<String> chunks = new ArrayList<>();
        for (TownBlock block : blocks)
            chunks.add(block.getX() + CHUNK_DELIMITER + block.getZ());

        return String.join(DELIMITER, chunks);

    }

    /**
     * Computes the stage times of a {@link Battle} based on its initial {@link TownBlock} count and a configurable multiplier.
     * @param b the {@link Battle} in question
     */
    public static Map<BattleStage, Duration> computeStageTimes(Battle b) {
        EnumMap<BattleStage, Duration> stageTimes = new EnumMap<>(BattleStage.class);

        int size = b.getInitialTownBlocks().size();

        stageTimes.put(BattleStage.PRE_FLAG, !b.isCityState() ? Duration.ofMinutes(Math.round(0.5*size)) : Duration.ofSeconds(30));
        stageTimes.put(BattleStage.FLAG, Duration.ofMinutes(Math.round(1.7*size)));
        stageTimes.put(BattleStage.RUINED, Duration.ofMinutes(Math.round(1.5*size)));
        stageTimes.put(BattleStage.DORMANT, Duration.ofDays((long) Math.ceil(size/30.0) + 1));

        return stageTimes;
    }

    /**
     * Returns the {@link BattleStage#PRE_FLAG} and {@link BattleStage#FLAG} durations added.
     * @param b the {@link Battle}.
     */
    public static Duration getActivePeriod(Battle b) {
        return b.getDuration(BattleStage.PRE_FLAG).plus(b.getDuration(BattleStage.FLAG));
    }
}
