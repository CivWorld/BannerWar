package io.github.townyadvanced.flagwar.battle_tracking.structures.occurrences;

import io.github.townyadvanced.flagwar.battle_tracking.util.SerializationUtil;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;

public record KillOccurrence(

    String killer,
    String killed,
    Material weapon, // issues of, say, knocking someone into a faller, and then they die while you're holding a pearl.
    long timeStamp
)
{
    public static KillOccurrence from(Entity killer, Entity killed, ItemStack weapon, EntityDamageEvent.DamageCause finalDamageCause) {
        return new KillOccurrence(killer != null ? killer.getName() : finalDamageCause.name(), killed.getName(), weapon.getType(), System.currentTimeMillis());
    }

    public static KillOccurrence from(String JSONString) {
        return (KillOccurrence) SerializationUtil.fromJson(JSONString, KillOccurrence.class);
    }

    public static Collection<KillOccurrence> deserialize(String delimitedJSONString) {
        Collection<KillOccurrence> out = new ArrayList<>();
        if (delimitedJSONString == null || delimitedJSONString.isEmpty()) return out;
        String[] jsonStrings = delimitedJSONString.split("\n");
        for (String jsonString : jsonStrings) {
            out.add(from(jsonString));
        }
        return out;
    }

    public static String toJSON(KillOccurrence killOccurrence) {
        return SerializationUtil.toJson(killOccurrence);
    }

    public static String serialize(Collection<KillOccurrence> killOccurrences) {
        Collection<String> jsonStrings = new ArrayList<>();
        for (KillOccurrence damageOccurrence : killOccurrences) {
            jsonStrings.add(toJSON(damageOccurrence));
        }
        return String.join("\n", jsonStrings);
    }}
