package io.github.townyadvanced.flagwar.battle_tracking.structures.enums;

import java.util.Locale;

public enum Affiliation {
    ATTACKER,
    DEFENDER,
    VAGRANT;

    public String properName() {
        return name().toLowerCase(Locale.ROOT).replace("_", " ");
    }
}
