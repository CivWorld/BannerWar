package io.github.townyadvanced.flagwar.battle_tracking.structures.occurrences;

import org.bukkit.entity.Entity;
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
}
