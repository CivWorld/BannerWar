package io.github.townyadvanced.flagwar.battle_tracking.model.enums;

import java.util.Locale;

/** The various statuses a battle can be in. */
public enum BattleStatus {

    /** The battle has not yet finished. */
    ONGOING,

    /** The battle has finished, concluding with the successfully attacker taking the home block of the defender. */
    ATTACKER_VICTORY,

    /** The battle has finished, concluding with the attacker failing to take the home block of the defender in time. */
    DEFENDER_VICTORY,

    /** The battle has finished prematurely, either due to an internal error or the collapsing of a relevant town/nation. */
    PREMATURELY_ENDED;

    /**
     * Returns the enum constant's {@link #name()} as a lowercase with its underscores replaced with spaces.
     * <p>For example, this method called on {@code EXAMPLE_ENUM_CONSTANT} would return "example enum constant"</p>
     */
    public String properName() {
        return name().toLowerCase(Locale.ROOT).replace("_", " ");
    }
}
