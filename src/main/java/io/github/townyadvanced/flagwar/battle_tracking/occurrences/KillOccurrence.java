package io.github.townyadvanced.flagwar.battle_tracking.occurrences;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public record KillOccurrence(

    UUID killer,
    UUID killed,
    Material weapon, // issues of, say, knocking someone into a faller, and then they die while you're holding a pearl.
    long timeStamp,
    EntityDamageEvent.DamageCause finalDamageCause
)
{
    public static KillOccurrence from(Entity killer, Entity killed, ItemStack weapon, EntityDamageEvent.DamageCause finalDamageCause) {
        return new KillOccurrence(killer.getUniqueId(), killed.getUniqueId(), weapon.getType(), System.currentTimeMillis(), finalDamageCause);
    }
}
