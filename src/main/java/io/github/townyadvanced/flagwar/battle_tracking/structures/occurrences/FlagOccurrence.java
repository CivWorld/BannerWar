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
    long lifeTime, // this value does NOT update.
    FlagResult result,
    String flagDestroyer

)
{
    public static FlagOccurrence create(String flagPlacer) {
        return new FlagOccurrence(
            flagPlacer,
            System.currentTimeMillis(),
            -1,
            FlagResult.ONGOING,
            Strings.EMPTY
        );
    }

    public FlagOccurrence completed(FlagResult result, String flagDestroyer) {
        return new FlagOccurrence(
            this.flagPlacer(),
            this.unixStartTime(),
            System.currentTimeMillis() - unixStartTime,
            result,
            flagDestroyer
        );
    }

    public static FlagOccurrence from(String JSONString) {
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
        for (var flagOccurrence : flagOccurrences) {
            jsonStrings.add(toJSON(flagOccurrence));
        }

        return String.join("\n", jsonStrings);
    }
}
