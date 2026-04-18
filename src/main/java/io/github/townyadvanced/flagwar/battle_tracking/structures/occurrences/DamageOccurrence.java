package io.github.townyadvanced.flagwar.battle_tracking.structures.occurrences;

import io.github.townyadvanced.flagwar.battle_tracking.util.SerializationUtil;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public record DamageOccurrence(
    UUID hurter,
    UUID hurted,
    double damage,
    long timeStamp

) {
    public static DamageOccurrence from(Entity hurter, Entity hurted, double damage) {
        return new DamageOccurrence(hurter.getUniqueId(), hurted.getUniqueId(), damage, System.currentTimeMillis());
    }

    public static DamageOccurrence from(String JSONString) {
        System.out.println(JSONString);
        return (DamageOccurrence) SerializationUtil.fromJson(JSONString, DamageOccurrence.class);
    }

    public static Collection<DamageOccurrence> deserialize(String delimitedJSONString) {
        Collection<DamageOccurrence> out = new ArrayList<>();
        if (delimitedJSONString == null || delimitedJSONString.isEmpty()) return out;
        String[] jsonStrings = delimitedJSONString.split("\n");
        for (String jsonString : jsonStrings) {
            out.add(from(jsonString));
        }
        return out;
    }

    public static String toJSON(DamageOccurrence damageOccurrence) {
        return SerializationUtil.toJson(damageOccurrence);
    }

    public static String serialize(Collection<DamageOccurrence> damageOccurrences) {
        Collection<String> jsonStrings = new ArrayList<>();
        for (DamageOccurrence damageOccurrence : damageOccurrences) {
            jsonStrings.add(toJSON(damageOccurrence));
        }
        return String.join("\n", jsonStrings);
    }
}
