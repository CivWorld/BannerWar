package io.github.townyadvanced.flagwar.battle_tracking.util;

import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import io.github.townyadvanced.flagwar.BannerWarAPI;
import io.github.townyadvanced.flagwar.objects.Battle;
import org.bukkit.util.BoundingBox;

import java.util.*;
import java.util.stream.Collectors;

/** See {@link #determineRegionFor(Town)}. */
public final class BattleRegionDeterminer {

    /**
     * A static method that constructs a new {@link BattleRegionDeterminer} and returns {@link #determineRegion()}.
     * @param town the town
     */
    public static Collection<BoundingBox> determineRegionFor(Town town) {
        return new BattleRegionDeterminer(town).determineRegion();
    }

    /** Holds the {@link Town} whose region is being determined. */
    private final Town TOWN;

    /** Holds the amount the war region will be expanded by to encompass fights straying away from a town. */
    private static final double EXPANSION = 64;

    /** Holds the maximum Y level of the world. */
    private final double MIN_Y;

    /** Holds the maximum Y level of the world. */
    private final double MAX_Y;

    /** Holds the {@link Set} of every town block that has been marked as flooded. */
    private final Set<WorldCoord> WORLD_COORDS = new HashSet<>();

    /** Holds the set of world coordinates that count as part of the town. */
    private final Set<WorldCoord> VALID_COORDS;

    private BattleRegionDeterminer(Town town) {
     MIN_Y = town.getWorld().getMinHeight();
     MAX_Y = town.getWorld().getMaxHeight();
     this.TOWN = town;
     Battle battle = BannerWarAPI.getBattle(TOWN);
     VALID_COORDS = battle != null ? (new HashSet<>(battle.getInitialTownBlocksAsWorldCoords()))
         : town.getTownBlocks().stream().map(TownBlock::getWorldCoord).collect(Collectors.toSet());
    }

    /**
     * Starts the process of determining the battle region of a town in the form of a list of bounding boxes.
     * @return the battle region of the town
     */
    private Collection<BoundingBox> determineRegion() {
         List<BoundingBox> battleRegions = new ArrayList<>();

         for (var coord : VALID_COORDS) {
             BoundingBox worldPart = new BoundingBox();
             battleRegions.add(worldPart);
             if (isNotTarget(coord)) continue;
             mark(worldPart, coord);
             flood(coord, worldPart);
         }

         battleRegions = mergeAndResize(battleRegions);
         return battleRegions;
     }

    /**
     * Initiates a 4-connected iterative flood fill of a town block, checking if {@link #isNotTarget(WorldCoord)}
     * returns {@code false} before marking is as a non-target and calling adjacent town blocks.
     * @param worldCoord the {@link WorldCoord} of the town block
     * @param box the bounding box to be resized if the town block is marked
     */
    private void flood(WorldCoord worldCoord, BoundingBox box) {
        Queue<WorldCoord> queue = new LinkedList<>();
        queue.add(worldCoord);
        WORLD_COORDS.add(worldCoord);

        while (!queue.isEmpty()) {
            WorldCoord current = queue.poll();

            // Check 4-connected neighbors: North, South, East, West
            int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
            for (int[] d : directions) {
                WorldCoord other = current.add(d[0], d[1]);

                if (!isNotTarget(other)) {
                    box = mark(box, worldCoord);
                    queue.add(other);
                }
            }
        }
    }

    private BoundingBox mark(BoundingBox box, WorldCoord worldCoord) {
        WORLD_COORDS.add(worldCoord);

        double minX = worldCoord.getX() * 16d;
        double minZ = worldCoord.getZ() * 16d;

        box = box.union(new BoundingBox(minX, MIN_Y, minZ, minX + 15, MAX_Y,  minZ + 15));
        return box;
    }

    /**
     * Returns whether a {@link TownBlock} has already been marked in {@link #WORLD_COORDS},
     * doesn't belong to the {@link #TOWN} or is {@code null}.
     * @param coord the {@link WorldCoord} of this town block
     */
    private boolean isNotTarget(WorldCoord coord) {
        return coord == null
            ||  coord.getTownBlockOrNull() == null
            || !TOWN.equals(coord.getTownBlockOrNull().getTownOrNull())
            || WORLD_COORDS.contains(coord);
     }

    /**
     * Takes every bounding box in a collection and merges ones that are adjacent or overlapping, while also
     * resizing the final result by expanding them by the {@link #EXPANSION}.
     * @param boxes the collection to be used for this
     */
    private static List<BoundingBox> mergeAndResize(List<BoundingBox> boxes) {
        if (boxes == null || boxes.size() <= 1) return boxes != null ? boxes : new ArrayList<>();

        boxes.sort(Comparator.comparingDouble(BoundingBox::getMinX));
        boolean[] merged = new boolean[boxes.size()];

        List<BoundingBox> mergedList = new ArrayList<>();

        for (int i = 0; i < boxes.size(); i++) {
            if (merged[i]) continue;

            BoundingBox current = boxes.get(i).clone();
            for (int j = i + 1; j < boxes.size(); j++) {
                if (merged[j]) continue;

                BoundingBox other = boxes.get(j);

                if (other.getMinX() > current.getMaxX() + 1) break;

                if (current.clone().expand(1, 0, 1).overlaps(other)) {
                    current.union(other);
                    merged[j] = true;
                    j = i;
                }
            }
            mergedList.add(current);
        }

        for (BoundingBox b : mergedList) b.expand(EXPANSION, 0, EXPANSION);

        return mergedList;
    }
}
