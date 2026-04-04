package io.github.townyadvanced.flagwar.chunk;

import io.github.townyadvanced.flagwar.FlagWar;
import io.github.townyadvanced.flagwar.config.BannerWarConfig;
import io.github.townyadvanced.flagwar.util.Broadcasts;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
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
        final int DEFAULT_BATCH_SIZE = 10;
        paste(PersistentChunk.of(chunks), world, DEFAULT_BATCH_SIZE);
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
                        pasteToWorld(batchForPasting, world)
                        .thenAccept(this::deleteFiles),
                    runnable -> SCHEDULER.runTask(PLUGIN, runnable))
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

                String fileName = ChunkHelper.fileNamefrom(pc.getX(), pc.getZ());
                File chunkFile = new File(
                    CHUNK_PATH.resolve(Path.of(fileName)).toString()
                );

                if (!chunkFile.exists()) continue;

                try (FileInputStream fis = new FileInputStream(chunkFile);
                     ObjectInputStream ois = new ObjectInputStream(fis)) {

                    pc.setMaterials((String[]) ois.readObject());
                    pc.setBlockData((String[]) ois.readObject());
                    out.add(pc);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return out;
        });
    }

    private CompletableFuture<Collection<PersistentChunk>>
        pasteToWorld(Deque<PersistentChunk> persistentChunksQueue, World world) {

        var CF = new CompletableFuture<Collection<PersistentChunk>>();
        var modifiableQueue = new ArrayDeque<>(persistentChunksQueue);

        final int CHUNKS_PER_TICK = 5;
        var blacklistedMaterials = BannerWarConfig.getBlacklistedMaterials();

        new BukkitRunnable() {
            @Override
            public void run() {
                for (int n = 0; n < CHUNKS_PER_TICK; n++) {

                    PersistentChunk pc;

                    if (!modifiableQueue.isEmpty()) pc = modifiableQueue.poll();
                    else {
                        CF.complete(persistentChunksQueue);
                        cancel(); return;
                    }

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

                        if (blacklistedMaterials.contains(newMat)) continue;
                        ChunkHelper.restoreBlock(thisBlock, newMat, newData);
                    }

                    for (var entity : thisChunk.getEntities()) {
                        if (entity instanceof Item)
                            entity.remove();

                        else if (entity instanceof LivingEntity
                            && ChunkHelper.checkUnSuffocate(entity)
                            && entity instanceof Player p)
                        {
                            Broadcasts.sendMessage(p,
                                "Teleported you to the top during chunk restoration!", ChatColor.YELLOW);
                        }
                    }
                }
            }
        }.runTaskTimer(PLUGIN, 0, 1);
        return CF;
    }

    private void deleteFiles(Collection<PersistentChunk> persistentChunks) {
        try {
            for (PersistentChunk pc : persistentChunks) {
                String fileName = ChunkHelper.fileNamefrom(pc.getX(), pc.getZ());
                Files.deleteIfExists(CHUNK_PATH.resolve(Path.of(fileName)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the {@link ChunkPaste#instance}.
     */
    public static ChunkPaste getInstance() {
        if (instance == null) {
            instance = new ChunkPaste(FlagWar.getFlagWar());
        }
        return instance;
    }
}
