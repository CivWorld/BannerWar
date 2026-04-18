package io.github.townyadvanced.flagwar.battle_tracking.structures.results;

import io.github.townyadvanced.flagwar.battle_tracking.TrackedBattle;
import io.github.townyadvanced.flagwar.battle_tracking.structures.enums.BattleResultEnum;
import io.github.townyadvanced.flagwar.battle_tracking.structures.occurrences.DamageOccurrence;

import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public record TrackedBattleResult(
    String townName,
    BattleResultEnum result,
    String attackerNationName,
    String defenderNationName,
    long unixStartTime,
    Duration battleDuration,
    Map<String, PlayerResult> playerResultMap,
    Collection<DamageOccurrence> damageOccurrences
) {

    public static TrackedBattleResult parse(TrackedBattle battle, BattleResultEnum result) {
        Map<String, PlayerResult> playerResultMap = new HashMap<>();

        for (var player : battle.getTrackedPlayers()) {
            String name = player.getOfflinePlayer().getName();
            PlayerResult playerResult = PlayerResult.parse(player);
            playerResultMap.put(name, playerResult);
        }

        return new TrackedBattleResult(
            battle.getTown().getName(),
            result,
            battle.getAttacker().getName(),
            battle.getDefender().getName(),
            battle.getStartTime(),
            Duration.ofMillis(System.currentTimeMillis() - battle.getStartTime()),
            playerResultMap,
            battle.getDamageOccurrences()
        );
    }

}
