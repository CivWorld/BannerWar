package io.github.townyadvanced.flagwar.battle_tracking.listeners;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import org.bukkit.util.BoundingBox;

import java.util.*;

/** See {@link #determineRegionFor(Town)}. */
public class BattleRegionDeterminer {

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

    /** Holds the maximum Y level minus the {@link #EXPANSION}, so that the entire chunk is covered upon resizing. */
    private final double MIN_Y;

    /** Holds the maximum Y level plus the {@link #EXPANSION}, so that the entire chunk is covered upon resizing. */
    private final double MAX_Y;

    /** Holds the {@link Map} of every town block that has been marked as flooded. */
    private final Map<WorldCoord, Boolean> MARKED_TOWN_BLOCKS = new HashMap<>();

    private BattleRegionDeterminer(Town town) {
     MIN_Y = town.getWorld().getMinHeight() + EXPANSION;
     MAX_Y = town.getWorld().getMaxHeight() - EXPANSION;
     this.TOWN = town;
    }

    /**
     * Starts the process of determining the battle region of a town in the form of a list of bounding boxes.
     * @return the battle region of the town
     */
    private Collection<BoundingBox> determineRegion() {

         List<BoundingBox> battleRegions = new ArrayList<>();

         for (var townBlock : TOWN.getTownBlocks()) {
             BoundingBox worldPart = new BoundingBox();
             floodFill(townBlock.getWorldCoord(), worldPart);
         }

         battleRegions = mergeAndResize(battleRegions);
         return battleRegions;
     }

    /**
     * Initiates a 4-connected recursive flood fill of a town blocks, checking if {@link #isNotTarget(TownBlock)}
     * returns {@code false} before marking is as a non-target and calling adjacent town blocks.
     * @param worldCoord the {@link WorldCoord} of the town block
     * @param box the bounding box to be resized if the town block is marked
     */
    private void floodFill(WorldCoord worldCoord, BoundingBox box) {
         var townBlock = TownyAPI.getInstance().getTownBlock(worldCoord);
         if (isNotTarget(townBlock)) return;

         MARKED_TOWN_BLOCKS.put(townBlock.getWorldCoord(), true);

         double minX = townBlock.getX() * 16d;
         double minZ = townBlock.getZ() * 16d;

         box = box.union(new BoundingBox(minX, MIN_Y, minZ, minX + 15, MAX_Y,  minZ + 15));

        floodFill(worldCoord.add(0, 1), box);
        floodFill(worldCoord.add(1, 0), box);
        floodFill(worldCoord.add(0, -1), box);
        floodFill(worldCoord.add(-1, 0), box);
     }

    /**
     * Returns whether a {@link TownBlock} has already been marked in {@link #MARKED_TOWN_BLOCKS},
     * doesn't belong to the {@link #TOWN} or is {@code null}.
     * @param townBlock the town block
     */
    private boolean isNotTarget(TownBlock townBlock) {
        return townBlock == null
            || townBlock.getTownOrNull() == null
            || !townBlock.getTownOrNull().equals(TOWN)
            || !MARKED_TOWN_BLOCKS.getOrDefault(townBlock.getWorldCoord(), false);
     }

    /**
     * Takes every bounding box in a collection and merges ones that are adjacent or overlapping, while also
     * resizing the final result by expanding them by the {@link #EXPANSION}.
     * @param boxes the collection to be used for this
     */
    public List<BoundingBox> mergeAndResize(List<BoundingBox> boxes) {
        if (boxes == null || boxes.isEmpty()) return new ArrayList<>();

        List<BoundingBox> mergedList = new ArrayList<>(boxes);
        boolean merged;

        do {
            merged = false;
            for (int i = 0; i < mergedList.size(); i++) {
                for (int j = i + 1; j < mergedList.size(); j++) {

                    BoundingBox box = mergedList.get(i);
                    BoundingBox other = mergedList.get(j);

                    if (box.clone().expand(1).overlaps(other)) {
                        box.union(other);
                        mergedList.remove(j);
                        merged = true;
                        break;
                    }
                }
                if (merged) break;
            }
        } while (merged);

        mergedList.forEach(b -> b.expand(64)); // resize to fit more than just the chunks there.

        return mergedList;
    }
}
