package io.github.townyadvanced.flagwar.battle_tracking.structures.enums;

import java.util.Locale;

public enum BattleResultEnum {
    ONGOING,
    ATTACKER_VICTORY,
    DEFENDER_VICTORY,
    PREMATURELY_ENDED; // usually because of a failed serialization or a nation/town collapsing.

    public String properName() {
        return name().toLowerCase(Locale.ROOT).replace("_", " ");
    }
}
