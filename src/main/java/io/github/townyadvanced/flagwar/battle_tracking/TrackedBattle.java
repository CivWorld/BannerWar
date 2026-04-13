package io.github.townyadvanced.flagwar.battle_tracking;

import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.HashSet;

class TrackedBattle {
    private final Collection<BoundingBox> BATTLE_REGION = new HashSet<>();

    public boolean isInBattleRegion(Vector position) {
        for (BoundingBox b : BATTLE_REGION)
            if (b.contains(position)) return true;

        return false;
    }
}
