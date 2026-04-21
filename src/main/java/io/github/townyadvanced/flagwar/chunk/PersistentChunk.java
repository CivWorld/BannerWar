package io.github.townyadvanced.flagwar.chunk;

import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Collection;

// perhaps a useless chunk is a chunk containing null info, and empty chunks contain empty arrays?
/**
 * An object of this class contains enough information to be utilized by {@link ChunkCopy} and {@link ChunkPaste}
 * to copy, store, and paste a snapshot of the {@link Chunk} it represents.
 * <p>
 * A {@link PersistentChunk} holds 5 pieces of information, which are as follows: <br>
 * - The X coordinate of its chunk, <br>
 * - the Z coordinate of its chunk, <br>
 * - the name of the {@link World} that it represents, <br>
 * - an array for the material of every block within this chunk, and <br>
 * - an array for the block data of every block within this chunk.
 * <p>
 * A {@link PersistentChunk} can be one of two types: useless or useful. It does not permanently remain of a type; it can cycle between the two.
 * <p>
 * A useless {@link PersistentChunk} is such that {@link #isUseless()} returns {@code true}.
 * This is when it does not contain any {@link #materials} or {@link #blockData},
 * only a pair of {@link #X} and {@link #Z} coordinates.
 * <p>
 * A useful {@link PersistentChunk} is such that {@link #isUseless()} returns {@code false} and is therefore not useless.
 * This is when it contains non-null {@link #materials} and/or {@link #blockData},
 * and is taken to have enough chunk information to be pasted back to the world.
 * <p>
 * Please note that a {@link PersistentChunk} that represents a {@link Chunk} completely filled with air will be taken to be useless.
 * This is because, due to optimizations, they store null {@link #materials} and {@link #blockData}.
 * This fits the conditions set by {@link #isUseless()}, resulting in a {@code true} status.
 */
class PersistentChunk {

    private String[] materials;
    private String[] blockData;
    private final int X;
    private final int Z;
    private final String WORLD_NAME;

    /**
     * Constructs a new {@link PersistentChunk} for chunk persistence.
     * @param X the {@link PersistentChunk}'s X coordinate
     * @param Z the {@link PersistentChunk}'s Z coordinate
     * @param mats the {@link PersistentChunk}'s materials.
     * @param bds the {@link PersistentChunk}'s block data.
     */
    PersistentChunk(String[] mats, String[] bds, int X, int Z, String worldName) {
        materials = mats;
        blockData = bds;
        this.X = X;
        this.Z = Z;
        this.WORLD_NAME = worldName;
    }

    /**
     * Constructs a new {@link PersistentChunk}, where {@link #isUseless()} returns true.
     * @param X the {@link PersistentChunk}'s X coordinate
     * @param Z the {@link PersistentChunk}'s Z coordinate
     * @param worldName the {@link PersistentChunk}'s {@link World}'s name.
     */
    PersistentChunk(int X, int Z, String worldName) {
        this(null, null, X, Z, worldName);
    }

    /**
     * Returns the X coordinate of the {@link PersistentChunk}.
     */
    public int getX() {return X;}

    /**
     * Returns the Z coordinate of the {@link PersistentChunk}.
     */
    public int getZ() {return Z;}

    /**
     * Returns the name of the world that this {@link PersistentChunk} resides in.
     */
    public String getWorldName() {return WORLD_NAME;}

    /**
     * Returns a {@link String} array of the materials of the {@link PersistentChunk}.
     */
    public String[] getMaterials() {return materials;}

    /**
     * Returns a {@link String} array of the block data of the {@link PersistentChunk}.
     */
    public String[] getBlockData() {return blockData;}

    /**
     * Sets the {@link String} array of the materials of the {@link PersistentChunk}.
     */
    public void setMaterials(String[] mats) {materials = mats;}

    /**
     * Sets the {@link String} array of the block data of the {@link PersistentChunk}.
     */
    public void setBlockData(String[] bds) {
        blockData = bds;}

    /**
     * Returns whether the {@link PersistentChunk} contains no block data and no materials.
     */
    public boolean isUseless() {
        return (materials == null && blockData == null);
    }

    /**
     * Returns a useless {@link PersistentChunk} from a {@link Chunk}.
     * @param chunk the {@link Chunk}
     */
    public static PersistentChunk of(Chunk chunk) {
        return new PersistentChunk(chunk.getX(), chunk.getZ(), chunk.getWorld().getName());
    }

    /**
     * Returns a {@link Collection} of useless {@link PersistentChunk}s from a {@link Chunk} {@link Collection}.
     * @param chunks the {@link Chunk} {@link Collection}
     */
    public static Collection<PersistentChunk> of(Collection<Chunk> chunks) {
        Collection<PersistentChunk> out = new ArrayList<>();

        for (Chunk c : chunks)
            out.add(of(c));

        return out;
    }
}
