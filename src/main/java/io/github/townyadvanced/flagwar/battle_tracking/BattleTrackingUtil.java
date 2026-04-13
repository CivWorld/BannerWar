package io.github.townyadvanced.flagwar.battle_tracking;

import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import org.bukkit.util.BoundingBox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

final class BattleTrackingUtil{

    public static Collection<BoundingBox> determineWarRegion(Town town) {
        var boundingBoxes = new ArrayList<>();

        var townBlocks = town.getTownBlocks();

        for (var block : townBlocks) {
            BoundingBox box = new BoundingBox();
            box = dpsTownBlock(box, block);
        }
    }


    private static BoundingBox dpsTownBlock(BoundingBox currentBox, TownBlock townBlock, Town town) {
        if (townBlock == null || townBlock.getTownOrNull() != null || !townBlock.getTownOrNull().equals(town)) {

        }
    }
}
