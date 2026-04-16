package io.github.townyadvanced.flagwar.battle_tracking.structures.occurrences;

import io.github.townyadvanced.flagwar.battle_tracking.structures.enums.FlagResult;
import org.apache.logging.log4j.util.Strings;

import java.time.Duration;

public record FlagOccurrence(
    String flagPlacer,
    long unixStartTime,
    Duration lifeTime, // this value does NOT update.
    FlagResult result,
    String flagDestroyer

)
{
    public static FlagOccurrence create(String flagPlacer) {
        return new FlagOccurrence(
            flagPlacer,
            System.currentTimeMillis(),
            Duration.ZERO,
            FlagResult.ONGOING,
            Strings.EMPTY
        );
    }

    public FlagOccurrence completed(FlagResult result, String flagDestroyer) {
        return new FlagOccurrence(
            this.flagPlacer(),
            this.unixStartTime(),
            Duration.ofMillis(System.currentTimeMillis() - this.unixStartTime()),
            result,
            flagDestroyer
        );
    }
}
