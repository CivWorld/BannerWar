package io.github.townyadvanced.flagwar.objects;

import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import io.github.townyadvanced.flagwar.FlagWar;
import io.github.townyadvanced.flagwar.util.BattleUtil;
import org.bukkit.Location;
import org.bukkit.World;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * A record of a battle that can be stored in persistent storage.
 * @param contestedTown the contested town's name
 * @param attacker the attacking nation's name
 * @param defender the defending nation's name
 * @param homeX the X coordinate of the homeblock
 * @param homeZ the Z coordinate of the homeblock
 * @param stageStartTime the time, in milliseconds, when the current stage started
 * @param isCityState whether the town this battle is hosted in is a CityState
 * @param stage the {@link BattleStage} that this battle is currently on
 * @param worldID the {@link UUID} of the world that this battle is hosted in
 * @param townBlocksCoords the {@link Collection} of the {@link WorldCoord} of all {@link TownBlock}s that the contested town accommodated before the battle began
 * @param initialMayorID the {@link UUID} of the resident who was mayor before the battle began
 * @param originalTownSpawn the town spawn that was set before the battle began
 * @param originalHomeBlockWorld the world name of the town homeblock before the battle began
 * @param originalOutposts the town outposts that were set before the battle began
 */
public record BattleRecord (
    String contestedTown,
    String attacker,
    String defender,
    int homeX,
    int homeZ,
    long stageStartTime,
    boolean isCityState,
    BattleStage stage,
    UUID worldID,
    Collection<WorldCoord> townBlocksCoords,
    UUID initialMayorID,
    LocationSnapshot originalTownSpawn,
    String originalHomeBlockWorld,
    Collection<OutpostSnapshot> originalOutposts
)
{
    public static BattleRecord of(Battle b) {
        try {
            return new BattleRecord(
                b.getContestedTown() == null ? "_" : b.getContestedTown().getName(),
                b.getAttacker() == null ? "_" : b.getAttacker().getName(),
                b.getDefender() == null ? "_" : b.getDefender().getName(),
                b.getHomeBlockCoords().getX(),
                b.getHomeBlockCoords().getZ(),
                b.getStageStartTime(),
                b.isCityState(),
                b.getCurrentStage(),
                b.getHomeBlockCoords().getBukkitWorld().getUID(),
                BattleUtil.toWorldCoords(b.getInitialTownBlocks()),
                b.getInitialMayor().getUUID(),
                b.getOriginalTownSpawn(),
                b.getOriginalHomeBlockWorld(),
                b.getOriginalOutposts()
            );
        } catch (Exception e)  {
            FlagWar.getInstance().getLogger().severe("Error while creating BattleRecord: " + e.getMessage()
            + ". Ending battle...");
            b.prematurelyEndBattle();
            return null;
        }
    }

    public static BattleRecord legacy(
        String contestedTown,
        String attacker,
        String defender,
        int homeX,
        int homeZ,
        long stageStartTime,
        boolean isCityState,
        BattleStage stage,
        UUID worldID,
        Collection<WorldCoord> townBlocksCoords,
        UUID initialMayorID
    ) {
        return new BattleRecord(
            contestedTown,
            attacker,
            defender,
            homeX,
            homeZ,
            stageStartTime,
            isCityState,
            stage,
            worldID,
            townBlocksCoords,
            initialMayorID,
            null,
            null,
            null
        );
    }

    public String homeBlockWorldOrFallback() {
        return originalHomeBlockWorld == null || originalHomeBlockWorld.isBlank() ? null : originalHomeBlockWorld;
    }

    public record LocationSnapshot(String worldName, UUID worldID, double x, double y, double z, float yaw, float pitch) {
        public static LocationSnapshot of(Location location) {
            if (location == null || location.getWorld() == null)
                return null;

            World world = location.getWorld();
            return new LocationSnapshot(
                world.getName(),
                world.getUID(),
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch()
            );
        }

        public Location toLocation() {
            World world = worldID == null ? null : org.bukkit.Bukkit.getWorld(worldID);
            if (world == null && worldName != null)
                world = org.bukkit.Bukkit.getWorld(worldName);
            if (world == null)
                return null;
            return new Location(world, x, y, z, yaw, pitch);
        }

        public String serialize() {
            return join(
                encode(worldName),
                worldID == null ? "" : worldID.toString(),
                Double.toString(x),
                Double.toString(y),
                Double.toString(z),
                Float.toString(yaw),
                Float.toString(pitch)
            );
        }

        public static LocationSnapshot deserialize(String serialized) {
            if (serialized == null || serialized.isBlank())
                return null;

            String[] parts = serialized.split("\\|", -1);
            if (parts.length < 7)
                return null;

            return new LocationSnapshot(
                decode(parts[0]),
                parts[1].isBlank() ? null : UUID.fromString(parts[1]),
                Double.parseDouble(parts[2]),
                Double.parseDouble(parts[3]),
                Double.parseDouble(parts[4]),
                Float.parseFloat(parts[5]),
                Float.parseFloat(parts[6])
            );
        }
    }

    public record OutpostSnapshot(String worldName, UUID worldID, int x, int z, LocationSnapshot spawn, String name) {
        public static OutpostSnapshot of(Location spawn, String name) {
            LocationSnapshot spawnSnapshot = LocationSnapshot.of(spawn);
            if (spawnSnapshot == null)
                return null;

            WorldCoord outpostCoord = WorldCoord.parseWorldCoord(spawn);
            return new OutpostSnapshot(
                outpostCoord.getWorldName(),
                spawn.getWorld().getUID(),
                outpostCoord.getX(),
                outpostCoord.getZ(),
                spawnSnapshot,
                name
            );
        }

        public WorldCoord toWorldCoord() {
            World world = worldID == null ? null : org.bukkit.Bukkit.getWorld(worldID);
            if (world != null)
                return new WorldCoord(world, x, z);
            if (worldID != null)
                return new WorldCoord(worldName, worldID, x, z);
            return new WorldCoord(worldName, x, z);
        }

        public String serialize() {
            return join(
                encode(worldName),
                worldID == null ? "" : worldID.toString(),
                Integer.toString(x),
                Integer.toString(z),
                encode(spawn == null ? "" : spawn.serialize()),
                encode(name)
            );
        }

        public static OutpostSnapshot deserialize(String serialized) {
            if (serialized == null || serialized.isBlank())
                return null;

            String[] parts = serialized.split("\\|", -1);
            if (parts.length < 6)
                return null;

            return new OutpostSnapshot(
                decode(parts[0]),
                parts[1].isBlank() ? null : UUID.fromString(parts[1]),
                Integer.parseInt(parts[2]),
                Integer.parseInt(parts[3]),
                LocationSnapshot.deserialize(decode(parts[4])),
                decode(parts[5])
            );
        }
    }

    public static String serializeOutposts(Collection<OutpostSnapshot> outposts) {
        if (outposts == null)
            return null;
        if (outposts.isEmpty())
            return "";

        List<String> serialized = new ArrayList<>();
        for (OutpostSnapshot outpost : outposts) {
            if (outpost != null)
                serialized.add(encode(outpost.serialize()));
        }
        return String.join(",", serialized);
    }

    public static Collection<OutpostSnapshot> deserializeOutposts(String serialized) {
        if (serialized == null)
            return null;
        if (serialized.isBlank())
            return List.of();

        List<OutpostSnapshot> outposts = new ArrayList<>();
        for (String encodedOutpost : serialized.split(",", -1)) {
            if (encodedOutpost.isBlank())
                continue;

            OutpostSnapshot outpost = OutpostSnapshot.deserialize(decode(encodedOutpost));
            if (outpost != null)
                outposts.add(outpost);
        }
        return outposts;
    }

    public static Collection<OutpostSnapshot> snapshotOutposts(Town town) {
        if (town == null || !town.hasOutpostSpawn())
            return List.of();

        List<Location> outpostSpawns = town.getAllOutpostSpawns();
        List<OutpostSnapshot> outposts = new ArrayList<>();

        for (Location outpostSpawn : outpostSpawns) {
            OutpostSnapshot outpost = OutpostSnapshot.of(outpostSpawn, getOutpostName(outpostSpawn));
            if (outpost != null)
                outposts.add(outpost);
        }

        return outposts;
    }

    private static String getOutpostName(Location outpostSpawn) {
        TownBlock townBlock = WorldCoord.parseWorldCoord(outpostSpawn).getTownBlockOrNull();
        if (townBlock == null)
            return "";
        if (townBlock.hasPlotObjectGroup())
            return townBlock.getPlotObjectGroup().getName();
        return townBlock.getName();
    }

    private static String join(String... parts) {
        return String.join("|", parts);
    }

    private static String encode(String value) {
        if (value == null)
            return "";
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String value) {
        if (value == null || value.isBlank())
            return "";
        return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    }
}
