package io.github.townyadvanced.flagwar.battle_tracking.model.enums;

import java.util.Locale;

/** The various statuses a flag can be in. */
public enum FlagStatus {

    /** The flag is still being fought over at this time. */
    ONGOING,

    /** The flag placer has successfully defended this flag until the cell is taken. */
    FLAG_SUCCESS,

    /** A flag destroyer has successfully destroyed this flag and prevented the cell from being taken. */
    FLAG_DEFENDED,

    /** The flag has prematurely ended, usually due to the server restarting/crashing or the battle ending mid-flag.*/
    ATTACK_CANCELLED;

    /**
     * Returns the enum constant's {@link #name()} as a lowercase with its underscores replaced with spaces.
     * <p>For example, this method called on {@code EXAMPLE_ENUM_CONSTANT} would return "example enum constant"</p>
     */
    public String properName() {
        return name().toLowerCase(Locale.ROOT).replace("_", " ");
    }
}
