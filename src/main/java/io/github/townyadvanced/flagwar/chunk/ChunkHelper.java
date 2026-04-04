package io.github.townyadvanced.flagwar.chunk;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.*;
import org.bukkit.entity.Entity;

final class ChunkHelper {
    private ChunkHelper() {}

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
            || d instanceof MultipleFacing
            || d instanceof Orientable
            || d instanceof AnaloguePowerable
            || d instanceof Attachable
            || d instanceof FaceAttachable
            || d instanceof Hangable
            || d instanceof Snowable;
    }
}
