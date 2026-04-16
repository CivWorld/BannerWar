package io.github.townyadvanced.flagwar.battle_tracking.util;

import com.google.gson.Gson;

/**
 * A utility class created in order to allow for serialization and deserialization, notably of an {@link Object} to and from a JSON {@link String}, respectively.
 * <br><br> Various static functions are provided in order to be called without the caller method's class having to instantiate a {@link Gson} itself.
 */
public final class SerializationUtil
{
    private SerializationUtil(){}

    /** Holds the {@link Gson} instance. */
    private static final Gson gson = new Gson();

    /**
     * Converts an {@link Object} to a serialized JSON {@link String}.
     * @param o the {@link Object} to be serialized
     * @return a JSON {@link String} to be stored in persistent storage
     */
    public static String toJson(Object o) { return gson.toJson(o); }

    /**
     * Converts a JSON {@link String} to an {@link Object}.
     * @param json the {@link String} to be deserialized
     * @return An {@link Object} for use in Java code
     */
    public static Object fromJson(String json) { return gson.fromJson(json, Object.class); }
}
