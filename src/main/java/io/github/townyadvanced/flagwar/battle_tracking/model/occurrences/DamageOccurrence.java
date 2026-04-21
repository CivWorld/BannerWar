package io.github.townyadvanced.flagwar.battle_tracking.model.occurrences;

import io.github.townyadvanced.flagwar.battle_tracking.util.SerializationUtil;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

/**
 * A record that logs a damage event.
 * @param hurter the name of the entity that dealt damage in this event
 * @param hurted the name of the entity that took damage in this event
 * @param damage the amount of damage dealt in this event
 * @param timeStamp the Unix epoch timestamp when the damage was taken
 */
public record DamageOccurrence(
    String hurter,
    String hurted,
    double damage,
    long timeStamp
) {

    /**
     * Returns a new {@link DamageOccurrence} from the arguments provided.
     * This should be used instead of the constructor in order to set the timestamp correctly.
     * @param hurter the name of the entity that dealt damage in this event
     * @param hurted the name of the entity that took damage in this event
     * @param damage the amount of damage dealt in this event
     */
    public static DamageOccurrence from(Entity hurter, Entity hurted, double damage) {
        return new DamageOccurrence(hurter.getName(), hurted.getName(), damage, System.currentTimeMillis());
    }

    /**
     * Returns a new {@link DamageOccurrence} from a JSON string.
     * @param JSONString the JSON string
     */
    public static DamageOccurrence from(String JSONString) {
        return (DamageOccurrence) SerializationUtil.fromJson(JSONString, DamageOccurrence.class);
    }

    /**
     * Returns the JSON string representation of this record.
     */
    public String toJSON() {
        return SerializationUtil.toJson(this);
    }

    /**
     * Deserializes a delimited JSON string into a collection of {@link DamageOccurrence}s.
     * @param delimitedJSONString the delimited JSON string
     */
    public static Collection<DamageOccurrence> deserialize(String delimitedJSONString) {
        Collection<DamageOccurrence> out = new ArrayList<>();
        if (delimitedJSONString == null || delimitedJSONString.isEmpty()) return out;
        String[] jsonStrings = delimitedJSONString.split("\n");
        for (String jsonString : jsonStrings) {
            DamageOccurrence damageOccurrence = (DamageOccurrence) SerializationUtil.fromJson(jsonString, DamageOccurrence.class);
            out.add(damageOccurrence);
        }
        return out;
    }

    /**
     * Serializes a collection of {@link DamageOccurrence}s into a delimited JSON string.
     * @param damageOccurrences the collection of {@link DamageOccurrence}s
     */
    public static String serialize(Collection<DamageOccurrence> damageOccurrences) {
        Collection<String> jsonStrings = new ArrayList<>();
        for (DamageOccurrence damageOccurrence : damageOccurrences) {
            jsonStrings.add(damageOccurrence.toJSON());
        }
        return String.join("\n", jsonStrings);
    }
}
