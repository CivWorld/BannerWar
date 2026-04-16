package io.github.townyadvanced.flagwar.battle_tracking.structures.occurrences;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

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
}
