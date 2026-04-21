package io.github.townyadvanced.flagwar.battle_tracking.model.occurrences;

import io.github.townyadvanced.flagwar.battle_tracking.util.SerializationUtil;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A record that logs a kill event.
 * @param killer the name of the entity/event that killed this player
 * @param killed the name of the player that died
 * @param weapon the weapon used to kill this player
 * @param timeStamp the Unix epoch timestamp when the kill occurred
 */
public record KillOccurrence(
    String killer,
    String killed,
    Material weapon, // issues of, say, knocking someone into a faller, and then they die while you're holding a pearl.
    long timeStamp
) {

    /**
     * Returns a new {@link DamageOccurrence} from the arguments provided.
     * This should be used instead of the constructor in order to set the timestamp correctly.
     * @param killer the name of the entity that killed this player
     * @param finalDamageCause the name of the event that killed this player (if {@code killer} is null, this is used instead)
     * @param killed the name of the player that died
     * @param weapon the weapon used to kill this player
     */
    public static KillOccurrence from(Entity killer, Entity killed, ItemStack weapon, EntityDamageEvent.DamageCause finalDamageCause) {
        return new KillOccurrence(killer != null ? killer.getName() : finalDamageCause.name(), killed.getName(), weapon.getType(), System.currentTimeMillis());
    }

    /**
     * Returns a new {@link KillOccurrence} from a JSON string.
     * @param JSONString the JSON string
     */
    public static KillOccurrence from(String JSONString) {
        return (KillOccurrence) SerializationUtil.fromJson(JSONString, KillOccurrence.class);
    }

    /**
     * Deserializes a delimited JSON string into a collection of {@link KillOccurrence}s.
     * @param delimitedJSONString the delimited JSON string
     */
    public static Collection<KillOccurrence> deserialize(String delimitedJSONString) {
        Collection<KillOccurrence> out = new ArrayList<>();
        if (delimitedJSONString == null || delimitedJSONString.isEmpty()) return out;
        String[] jsonStrings = delimitedJSONString.split("\n");
        for (String jsonString : jsonStrings) {
            out.add(from(jsonString));
        }
        return out;
    }

    /**
     * Returns the JSON string representation of this record.
     */
    public String toJSON() {
        return SerializationUtil.toJson(this);
    }

    /**
     * Serializes a collection of {@link KillOccurrence}s into a delimited JSON string.
     * @param killOccurrences the collection of {@link KillOccurrence}s
     */
    public static String serialize(Collection<KillOccurrence> killOccurrences) {
        Collection<String> jsonStrings = new ArrayList<>();
        for (KillOccurrence killOccurrence : killOccurrences) {
            jsonStrings.add(killOccurrence.toJSON());
        }
        return String.join("\n", jsonStrings);
    }}
