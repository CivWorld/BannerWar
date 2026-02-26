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

public class ChunkPaste {

    /** Holds the instance of this class. */
    private static ChunkPaste INSTANCE;

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

    public void paste(Collection<Chunk> chunks, World world) {
        paste(PendingChunk.of(chunks), world, 10);
    }

    public void paste(Collection<PendingChunk> pendingChunks, World world, int batchSize) {

        Deque<PendingChunk> pendingChunksQueue = new ArrayDeque<>(pendingChunks);

        CompletableFuture<Void> future = CompletableFuture.completedFuture(null);

        do {
            int trueBatchSize = Math.min(batchSize, pendingChunksQueue.size());

            Deque<PendingChunk> batch = new ArrayDeque<>();
            for (int i = 0; i < trueBatchSize; i++) batch.add(pendingChunksQueue.poll());


            future = future.thenCompose(v ->
                readPendingChunks(batch)
                    .thenAcceptAsync(batchForPasting ->
                            pastePendingChunks(batchForPasting, world),
                        runnable -> SCHEDULER.runTask(PLUGIN, runnable)
                    )
            );
        } while (!pendingChunksQueue.isEmpty());
    }

    private CompletableFuture<Deque<PendingChunk>> readPendingChunks(Deque<PendingChunk> pendingChunksQueue) {

        return CompletableFuture.supplyAsync(() -> {

            Deque<PendingChunk> out = new ArrayDeque<>();

            int originalSize = pendingChunksQueue.size();

            for (int i = 0; i < originalSize; i++) {

                PendingChunk pc = pendingChunksQueue.poll();
                if (pc == null) break;

                File chunkFile = new File(
                    CHUNK_PATH.resolve(Path.of(pc.getX() + "_" + pc.getZ())).toString()
                );

                if (!chunkFile.exists()) {
                    continue;
                }
                try {
                    try (FileInputStream fis = new FileInputStream(chunkFile);
                         ObjectInputStream ois = new ObjectInputStream(fis)) {

                        pc.setMaterials((String[]) ois.readObject());
                        pc.setBlockDatas((String[]) ois.readObject());
                        out.add(pc);

                        System.out.println(pc.getX() + " . " + pc.getZ());

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

    private void pastePendingChunks(Deque<PendingChunk> pendingChunksQueue, World world) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (int n = 0; n < 5; n++) { // TODO: MAKE CONFIGURABLE

                    PendingChunk pc;

                    if (!pendingChunksQueue.isEmpty()) pc = pendingChunksQueue.poll();
                    else {cancel(); return;}

                    if (pc.isUseless()) {
                        LOGGER.warning("Chunk " + pc.getX() + " " + pc.getZ() + " has no information!");
                        continue;
                    }

                    Chunk thisChunk = world.getChunkAt(pc.getX(), pc.getZ());

                    for (int i = 0; i < pc.getBlockDatas().length; i++) {
                        int x = i % 16;
                        int z = (i / 16) % 16;
                        int y = (i) / 256;
                        int ny = y - 64;

                        Block thisBlock = thisChunk.getBlock(x, ny, z);

                        Material newMat = Material.getMaterial(pc.getMaterials()[i]);
                        String newData = pc.getBlockDatas()[i];
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
     * Returns the {@link ChunkPaste#INSTANCE}.
     */
    public static ChunkPaste getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ChunkPaste(JavaPlugin.getProvidingPlugin(FlagWar.class));
        }
        return INSTANCE;
    }
}
