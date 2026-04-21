package io.github.townyadvanced.flagwar.battle_tracking.model.enums;

import java.util.Locale;

/** The various ways a player can play a part in a battle. */
public enum Affiliation {

    /** Member of the attacking nation or an associated figure. */
    ATTACKER,

    /** Member of the defending nation or an associated figure. */
    DEFENDER,

    /** Member of neither nations nor their allies, and thus a third party. */
    VAGRANT;

    /**
     * Returns the enum constant's {@link #name()} as a lowercase with its underscores replaced with spaces.
     * <p>For example, this method called on {@code EXAMPLE_ENUM_CONSTANT} would return "example enum constant"</p>
     */
    public String properName() {
        return name().toLowerCase(Locale.ROOT).replace("_", " ");
    }
}
