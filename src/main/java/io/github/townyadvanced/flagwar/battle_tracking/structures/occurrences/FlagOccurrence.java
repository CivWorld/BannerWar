package io.github.townyadvanced.flagwar.battle_tracking.structures.occurrences;

import io.github.townyadvanced.flagwar.battle_tracking.structures.enums.FlagResult;
import io.github.townyadvanced.flagwar.battle_tracking.util.SerializationUtil;
import org.apache.logging.log4j.util.Strings;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;

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

    public static FlagOccurrence from(String JSONString) {
        System.out.println(JSONString);
        return (FlagOccurrence) SerializationUtil.fromJson(JSONString, FlagOccurrence.class);
    }

    public static Collection<FlagOccurrence> deserialize(String delimitedJSONString) {
        Collection<FlagOccurrence> out = new ArrayList<>();
        if (delimitedJSONString == null || delimitedJSONString.isEmpty()) return out;
        String[] jsonStrings = delimitedJSONString.split("\n");
        for (String jsonString : jsonStrings) {
            out.add(from(jsonString));
        }
        return out;
    }

    public static String toJSON(FlagOccurrence flagOccurrence) {
        return SerializationUtil.toJson(flagOccurrence);
    }

    public static String serialize(Collection<FlagOccurrence> flagOccurrences) {
        Collection<String> jsonStrings = new ArrayList<>();
        for (FlagOccurrence damageOccurrence : flagOccurrences) {
            jsonStrings.add(toJSON(damageOccurrence));
        }
        return String.join("\n", jsonStrings);
    }
}
