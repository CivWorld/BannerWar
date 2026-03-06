package io.github.townyadvanced.flagwar.chunk;

import io.github.townyadvanced.flagwar.FlagWar;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Item;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/** A class designed to paste chunks' {@link Material}s and {@link BlockData} in batches,
 * working in tandem with {@link ChunkCopy} to achieve chunk persistence. **/
public final class ChunkPaste {

    /** Holds the instance of this class. */
    private static ChunkPaste instance;

    /** Holds the {@link JavaPlugin} instance. */
    private final JavaPlugin PLUGIN;

    /** Holds the {@link Logger} of this class. */
    private final Logger LOGGER;

    /** Holds the {@link BukkitScheduler} instance. */
    private final BukkitScheduler SCHEDULER = Bukkit.getScheduler();

    /** Holds the path of the chunk folder to store chunks in. */
    private final Path CHUNK_PATH;

    /** Holds every {@link Material} that should not be restored. */
    private final Set<Material> BLACKLISTED_MATERIALS = Set.of(Material.DIAMOND_ORE);

    public ChunkPaste(JavaPlugin plugin) {
        this.PLUGIN = plugin;
        this.LOGGER = plugin.getLogger();
        this.CHUNK_PATH = plugin.getDataFolder().toPath().resolve("chunks");
    }

    /**
     * Reads the {@link PersistentChunk}s representing the {@link Chunk}s from the {@link #CHUNK_PATH}, and pastes them onto the world.
     * @param chunks the {@link Collection} of {@link Chunk}s whose {@link PersistentChunk}s will be pasted
     * @param world the {@link World} where the chunks will be pasted
     */
    public void paste(Collection<Chunk> chunks, World world) {
        paste(PersistentChunk.of(chunks), world, 10);
    }

    /**
     * Reads the {@link PersistentChunk}s from the {@link #CHUNK_PATH}, and pastes them onto the world.
     * @param persistentChunks the {@link PersistentChunk}s
     * @param world the {@link World} where the chunks will be pasted
     * @param batchSize how many chunks are pasted each batch
     */
    public void paste(Collection<PersistentChunk> persistentChunks, World world, int batchSize) {

        Deque<PersistentChunk> persistentChunksQueue = new ArrayDeque<>(persistentChunks);

        CompletableFuture<Void> future = CompletableFuture.completedFuture(null);

        do {
            int trueBatchSize = Math.min(batchSize, persistentChunksQueue.size());

            Deque<PersistentChunk> batch = new ArrayDeque<>();
            for (int i = 0; i < trueBatchSize; i++) batch.add(persistentChunksQueue.poll());


            future = future.thenCompose(v ->
                readFromFile(batch)
                    .thenAcceptAsync(batchForPasting ->
                            pasteToWorld(batchForPasting, world),
                        runnable -> SCHEDULER.runTask(PLUGIN, runnable)
                    )
            );
        } while (!persistentChunksQueue.isEmpty());
    }

    private CompletableFuture<Deque<PersistentChunk>> readFromFile(Deque<PersistentChunk> persistentChunksQueue) {

        return CompletableFuture.supplyAsync(() -> {

            Deque<PersistentChunk> out = new ArrayDeque<>();

            int originalSize = persistentChunksQueue.size();

            for (int i = 0; i < originalSize; i++) {

                PersistentChunk pc = persistentChunksQueue.poll();
                if (pc == null) break;

                File chunkFile = new File(
                    CHUNK_PATH.resolve(Path.of(pc.getX() + "_" + pc.getZ())).toString()
                );

                if (!chunkFile.exists()) continue;

                try {
                    try (FileInputStream fis = new FileInputStream(chunkFile);
                         ObjectInputStream ois = new ObjectInputStream(fis)) {

                        pc.setMaterials((String[]) ois.readObject());
                        pc.setBlockData((String[]) ois.readObject());
                        out.add(pc);

                    } catch (Exception e) {
                        LOGGER.severe(e.getMessage());
                    }

                    Files.deleteIfExists(chunkFile.toPath());

                } catch (Exception e) {
                    LOGGER.severe(e.getMessage());
                }
            }
            return out;
        });
    }

    private void pasteToWorld(Deque<PersistentChunk> persistentChunksQueue, World world) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (int n = 0; n < 5; n++) {

                    PersistentChunk pc;

                    if (!persistentChunksQueue.isEmpty()) pc = persistentChunksQueue.poll();
                    else {cancel(); return;}

                    if (pc.isUseless()) {
                        LOGGER.warning("Chunk " + pc.getX() + " " + pc.getZ() + " has no information!");
                        continue;
                    }

                    Chunk thisChunk = world.getChunkAt(pc.getX(), pc.getZ());

                    for (int i = 0; i < pc.getBlockData().length; i++) {
                        int x = i % 16;
                        int z = (i / 16) % 16;
                        int y = (i) / 256;
                        int ny = y - 64;

                        Block thisBlock = thisChunk.getBlock(x, ny, z);

                        Material newMat = Material.getMaterial(pc.getMaterials()[i]);
                        String newData = pc.getBlockData()[i];
                        if (newMat == null) thisBlock.setType(Material.AIR);

                        else {
                            if (BLACKLISTED_MATERIALS.contains(newMat)) continue;

                            thisBlock.setType(newMat);

                            if (newData != null)
                                thisBlock.setBlockData(Bukkit.createBlockData(newData));
                        }
                    }

                    for (var entity : thisChunk.getEntities())
                        if (entity instanceof Item) entity.remove();
                }
            }
        }.runTaskTimer(PLUGIN, 0, 1);
    }

    /**
     * Returns the {@link ChunkPaste#instance}.
     */
    public static ChunkPaste getInstance() {
        if (instance == null) {
            instance = new ChunkPaste(JavaPlugin.getProvidingPlugin(FlagWar.class));
        }
        return instance;
    }
}
