package io.github.HeroGame.utils;

/**
 * Simple interface for objects that can be serialized/deserialized to/from JSON.
 * Primarily for use with GSON, actual GSON usage will be more direct.
 */
public interface JsonSerializable {
    String toJson();
    void fromJson(String json);
}
