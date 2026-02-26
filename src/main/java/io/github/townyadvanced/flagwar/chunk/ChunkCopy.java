package io.github.townyadvanced.flagwar.chunk;

import io.github.townyadvanced.flagwar.FlagWar;
import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.block.data.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import java.util.logging.Logger;

public final class ChunkCopy {

    /** Holds the instance of this class. */
    private static ChunkCopy INSTANCE;

    /** Holds the {@link JavaPlugin} instance. */
    private final JavaPlugin PLUGIN;

    /** Holds the {@link BukkitScheduler} instance. */
    private final BukkitScheduler SCHEDULER = Bukkit.getScheduler();

    /** Holds the {@link java.util.logging.Logger} of this class. */
    private final Logger LOGGER;

    /** Holds the path of the chunk folder to store chunks in. */
    private final Path CHUNK_PATH;

    public ChunkCopy(JavaPlugin plugin) {
        this.PLUGIN = plugin;
        this.LOGGER = plugin.getLogger();
        this.CHUNK_PATH = plugin.getDataFolder().toPath().resolve("chunks");
    }

    /**
     * Copies the {@link Collection} of chunks as separate files to the chunk folder for later pasting.
     * @param chunks the {@link Collection} of chunks
     */
    public void copy(Collection<ChunkSnapshot> chunks) {

        for (var c : chunks) {

            String[] materials = new String[16 * 16 * 384];
            String[] blockDatas = new String[16 * 16 * 384];

            for (int x = 0; x < 16; x++)
                for (int z = 0; z < 16; z++)
                    for (int y = -64; y < 320; y++) {

                        int ny = y + 64; // it's going to otherwise be out of bounds because y is negative
                        BlockData d = c.getBlockData(x, y, z);

                        if (c.getBlockType(x, y, z) != Material.AIR) {
                            materials[x + (z * 16) + (ny * 16 * 16)] = c.getBlockType(x, y, z).toString();

                            if (isBlockDataUseful(d))
                                blockDatas[x + (z * 16) + (ny * 16 * 16)] = d.getAsString();
                        }
                    }

            SCHEDULER.runTaskAsynchronously(PLUGIN, () -> {
                // test if this works and if not go back to the fw one.
                File chunkFile = new File(CHUNK_PATH.resolve(Path.of(c.getX() + "_" + c.getZ())).toString());
                chunkFile.getParentFile().mkdirs();

                try (FileOutputStream fos = new FileOutputStream(chunkFile);
                     ObjectOutputStream oos = new ObjectOutputStream(fos)) {

                    oos.writeObject(materials);
                    oos.writeObject(blockDatas);

                } catch (IOException e) {LOGGER.warning(e.getMessage());}
            });
        }
    }

    /**
     * Returns whether the {@link BlockData} instance provides more information than just a material.
     * @param d the {@link BlockData}
     */
    private boolean isBlockDataUseful(BlockData d) {
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

    /**
     * Returns the {@link ChunkCopy#INSTANCE}.
     */
    public static ChunkCopy getInstance() {

        if (INSTANCE == null) {
            INSTANCE = new ChunkCopy(JavaPlugin.getProvidingPlugin(FlagWar.class));
            return INSTANCE;
        }

        return INSTANCE;
    }
}
