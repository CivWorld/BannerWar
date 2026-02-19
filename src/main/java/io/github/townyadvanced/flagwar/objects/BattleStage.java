package io.github.townyadvanced.flagwar.objects;

public enum BattleStage {
    PRE_FLAG, // the battle has begun; flags are not yet allowed to be placed.
    FLAG, // the battle is in its main stage; flags are being placed and the town is being contested.
    RUINED, // the battle has been lost; the town is in ruins and the attacker has free-range.
    DORMANT // the battle has ended, and the town cannot be attacked again until it leaves this dormant stage.
}
