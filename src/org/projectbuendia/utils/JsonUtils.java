package org.projectbuendia.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class JsonUtils {
    /** An instance of Gson with our preferred configuration. */
//    public static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Converts a JsonElement to a String, a Long, or null. If the JsonElement
     * is something other than a JSON string, a JSON number with an integer
     * value, or a JSON null, throws InvalidInputException.
     */
    public static Object toStringOrLongOrNull(JsonElement element)
        throws InvalidInputException {
        if (element.isJsonNull()) return null;
        if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isString()) {
                return primitive.getAsString();
            }
            if (primitive.isNumber()) {
                try {
                    return primitive.getAsLong();
                } catch (Exception e) {
                    // Gson 2.3 documentation is inconsistent about whether
                    // getAsLong throws NumberFormatException or
                    // ClassCastException.  For safety, catch all exceptions.
                    throw new InvalidInputException();
                }
            }
        }
        throw new InvalidInputException();
    }
}
