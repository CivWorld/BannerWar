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
import java.util.logging.Logger;

/** A class designed to copy chunks' {@link Material}s and {@link BlockData},
 * working in tandem with {@link ChunkPaste} to achieve chunk persistence. **/
public final class ChunkCopy {

    /** Holds the instance of this class. */
    private static ChunkCopy instance;

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

                        int i = x + (z * 16) + (ny * 16 * 16);

                        if (c.getBlockType(x, y, z) != Material.AIR) {
                            materials[i] = c.getBlockType(x, y, z).toString();

                            if (ChunkHelper.isBlockDataUseful(d))
                                blockDatas[i] = d.getAsString();
                        }
                    }

            SCHEDULER.runTaskAsynchronously(PLUGIN, () -> {
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
     * Returns the {@link ChunkCopy#instance}.
     */
    public static ChunkCopy getInstance() {

        if (instance == null) {
            instance = new ChunkCopy(FlagWar.getFlagWar());
            return instance;
        }

        return instance;
    }
}
