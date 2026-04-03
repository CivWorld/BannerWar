package io.github.townyadvanced.flagwar.chunk;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.*;
import org.bukkit.block.data.type.Bed;
import org.bukkit.entity.Entity;

final class ChunkHelper {
    private ChunkHelper() {}

    /**
     * Pastes the new {@link BlockData} to the block provided,
     * updating physics if the block is not a {@link Bisected} or a {@link Bed}.
     * @param b the block
     * @param blockData the block data
     * @return true if it is a {@link Bed.Part#FOOT} or a {@link Bisected.Half#BOTTOM}
     */
    static boolean checkPasteBlock(Block b, BlockData blockData) {
        if (blockData instanceof Bisected bct) {
            if (bct.getHalf() == Bisected.Half.BOTTOM) return true;
            else b.setBlockData(blockData, false);
            return false;
        }

        else if  (blockData instanceof Bed bed) {
            if (bed.getPart() == Bed.Part.FOOT) return true;
            else b.setBlockData(blockData, false);
            return false;
        }

        else {
            b.setBlockData(blockData);
            return false;
        }
    }

    /**
     * Checks if an entity's eye level is in a block (it is likely suffocating in said block),
     * and teleports it to the highest point of its X-Z coordinates if so.
     * @param e the entity
     * @return whether the entity had to be teleported upward
     */
    static boolean checkUnSuffocate(Entity e) {
        World world = e.getWorld();
        Location topOfHead = e.getLocation().clone().add(0, e.getHeight(), 0);

        if (topOfHead.getBlock().getType().isSolid()) {
            int highestY = world.getHighestBlockYAt(topOfHead.getBlockX(), topOfHead.getBlockZ()) + 1;
            e.teleport(
                new Location(
                    world,
                    topOfHead.getX(),
                    highestY,
                    topOfHead.getZ(),
                    topOfHead.getYaw(),
                    topOfHead.getPitch()
                )
            );
            return true;
        }
        return false;
    }

    /**
     * Returns whether the {@link BlockData} instance provides more information than just a material.
     * @param d the {@link BlockData}
     */
    static boolean isBlockDataUseful(BlockData d) {
        return d instanceof Directional
            || d instanceof Ageable
            || d instanceof Waterlogged
            || d instanceof Powerable
            || d instanceof Openable
            || d instanceof Bisected
            || d instanceof Lightable
            || d instanceof Levelled
            || d instanceof Rotatable
            || d instanceof MultipleFacing;
    }
}
