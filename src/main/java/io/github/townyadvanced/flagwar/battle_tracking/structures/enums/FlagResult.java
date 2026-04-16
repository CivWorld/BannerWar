package io.github.townyadvanced.flagwar.battle_tracking.structures.enums;

import java.util.Locale;

public enum FlagResult {
    ONGOING,
    FLAG_SUCCESS,
    FLAG_DEFENDED,
    ATTACK_CANCELLED; // usually because of a restart or crash.

    public String properName() {
        return name().toLowerCase(Locale.ROOT).replace("_", " ");
    }
}
