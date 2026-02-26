package io.github.townyadvanced.flagwar.chunk;

import org.bukkit.Chunk;

import java.util.ArrayList;
import java.util.Collection;

public class PendingChunk {

    private String[] materials;
    private String[] blockDatas;
    private final int X;
    private final int Z;

    PendingChunk(String[] mats, String[] bds, int X, int Z) {
        materials = mats;
        blockDatas = bds;
        this.X = X;
        this.Z = Z;
    }

    PendingChunk(int X, int Z) {
        materials = null;
        blockDatas = null;
        this.X = X;
        this.Z = Z;
    }

    /**
     * Returns the X coordinate of the {@link PendingChunk}.
     */
    public int getX() {return X;}

    /**
     * Returns the Z coordinate of the {@link PendingChunk}.
     */
    public int getZ() {return Z;}

    /**
     * Returns a {@link String} array of the materials of the {@link PendingChunk}.
     */
    public String[] getMaterials() {return materials;}

    /**
     * Returns a {@link String} array of the block data of the {@link PendingChunk}.
     */
    public String[] getBlockDatas() {return blockDatas;}

    /**
     * Sets the {@link String} array of the materials of the {@link PendingChunk}.
     */
    public void setMaterials(String[] mats) {materials = mats;}

    /**
     * Sets the {@link String} array of the block data of the {@link PendingChunk}.
     */
    public void setBlockDatas(String[] bds) {blockDatas = bds;}

    /**
     * Returns whether the {@link PendingChunk} contains no block data and no materials.
     * Please note that this means that, if a {@link PendingChunk} is completely filled with air, it will be taken to be useless.
     */
    public boolean isUseless() {
        return (materials == null && blockDatas == null);
    }

    /**
     * Returns a useless {@link PendingChunk} from a {@link Chunk}.
     * @param chunk the {@link Chunk}
     */
    public static PendingChunk of(Chunk chunk) {
        return new PendingChunk(chunk.getX(), chunk.getZ());
    }

    /**
     * Returns a {@link Collection} of useless {@link PendingChunk}s from a {@link Chunk} {@link Collection}.
     * @param chunks the {@link Chunk} {@link Collection}
     */
    public static Collection<PendingChunk> of(Collection<Chunk> chunks) {
        Collection<PendingChunk> out = new ArrayList<>();

        for (Chunk c : chunks)
            out.add(of(c));

        return out;
    }
}
