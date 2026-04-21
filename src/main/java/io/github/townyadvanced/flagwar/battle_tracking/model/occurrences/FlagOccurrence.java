package io.github.townyadvanced.flagwar.battle_tracking.model.occurrences;

import io.github.townyadvanced.flagwar.battle_tracking.model.enums.FlagStatus;
import io.github.townyadvanced.flagwar.battle_tracking.util.SerializationUtil;
import org.apache.logging.log4j.util.Strings;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A record that logs a flag event.
 * @param flagPlacer the name of the player that placed this flag
 * @param unixStartTime the Unix epoch timestamp when the flag was placed
 * @param lifeTime the lifetime, in milliseconds, of this flag
 * @param result the status of the flag
 * @param flagDestroyer the name of the player that destroyed this flag (this can be null if the flag has not been destroyed yet)
 */
public record FlagOccurrence(
    String flagPlacer,
    long unixStartTime,
    long lifeTime, // this value does NOT update.
    FlagStatus result,
    String flagDestroyer
) {

    /**
     * Returns a new incomplete {@link FlagOccurrence} from the arguments provided, that awaits completion via {@link #completed(FlagStatus, String)}.
     * This should be used instead of the constructor in order to set the timestamp and status correctly.
     * @param flagPlacer the name of the player that placed this flag
     */
    public static FlagOccurrence create(String flagPlacer) {
        return new FlagOccurrence(
            flagPlacer,
            System.currentTimeMillis(),
            -1,
            FlagStatus.ONGOING,
            Strings.EMPTY
        );
    }

    /**
     * Returns a completed version of this flag record (such that the lifetime is over)
     * @param result the status of this flag
     * @param flagDestroyer the player that destroyed this flag (nullable)
     */
    public FlagOccurrence completed(FlagStatus result, String flagDestroyer) {
        return new FlagOccurrence(
            this.flagPlacer(),
            this.unixStartTime(),
            System.currentTimeMillis() - unixStartTime,
            result,
            flagDestroyer
        );
    }

    /**
     * Returns a new {@link FlagOccurrence} from a JSON string.
     * @param JSONString the JSON string
     */
    public static FlagOccurrence from(String JSONString) {
        return (FlagOccurrence) SerializationUtil.fromJson(JSONString, FlagOccurrence.class);
    }

    /**
     * Returns the JSON string representation of this record.
     */
    public String toJSON() {
        return SerializationUtil.toJson(this);
    }

    /**
     * Deserializes a delimited JSON string into a collection of {@link FlagOccurrence}s.
     * @param delimitedJSONString the delimited JSON string
     */
    public static Collection<FlagOccurrence> deserialize(String delimitedJSONString) {
        Collection<FlagOccurrence> out = new ArrayList<>();
        if (delimitedJSONString == null || delimitedJSONString.isEmpty()) return out;
        String[] jsonStrings = delimitedJSONString.split("\n");
        for (String jsonString : jsonStrings) {
            out.add(from(jsonString));
        }
        return out;
    }

    /**
     * Serializes a collection of {@link FlagOccurrence}s into a delimited JSON string.
     * @param flagOccurrences the collection of {@link FlagOccurrence}s
     */
    public static String serialize(Collection<FlagOccurrence> flagOccurrences) {
        Collection<String> jsonStrings = new ArrayList<>();
        for (var flagOccurrence : flagOccurrences) {
            jsonStrings.add(flagOccurrence.toJSON());
        }

        return String.join("\n", jsonStrings);
    }
}
