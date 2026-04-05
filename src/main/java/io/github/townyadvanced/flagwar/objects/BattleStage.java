package io.github.townyadvanced.flagwar.objects;

/**
 * Holds one of the various stages a {@link Battle} could be in. <p>
 * Note that not all {@link BattleStage}s are guaranteed to be accessed in a battle.
 * For example, a battle that is won by the defender will never reach the {@link #RUINED} stage.
 */
public enum BattleStage {

    /** The battle has begun; flags are not yet allowed to be placed. */
    PRE_FLAG,

    /** The battle is in its main stage; flags are being placed and the town is being contested. */
    FLAG,

    /** The battle has been lost; the town is in ruins and the attacker has full access to the town. */
    RUINED,

    /** The battle has effectively ended, and the town cannot be attacked again until it leaves this dormant stage. */
    DORMANT,

    /** The battle is out of its dormant stage; BannerWar is no longer keeping track of this town, and it can be attacked again. */
    END
}
