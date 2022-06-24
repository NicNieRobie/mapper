package dev.vpendischuk.mapper.json.util;

import dev.vpendischuk.mapper.json.exceptions.JsonReadException;
import dev.vpendischuk.mapper.json.types.JsonValue;

/**
 * Represents a common interface for JSON readers -
 *   classes responsible for reading and partially
 *   parsing JSONs from strings and streams.
 */
public interface JsonReader {
    /**
     * Obtains the next character in the JSON, disregarding the whitespaces.
     *
     * @return the next character in the JSON, disregarding the whitespaces.
     */
    char nextCharacterTrimmed();

    /**
     * Returns the next value parsed from a JSON.
     *
     * @return {@link JsonValue} parsed from the next text value in the JSON.
     */
    JsonValue nextValue();

    /**
     * Returns the character that was previously read from JSON.
     *
     * @return the previously read character.
     */
    char previousCharacter();

    /**
     * Moves a character back in the JSON reading process.
     *
     * @throws JsonReadException if the method was used twice without moving forward once -
     *                           stepping back can be used only for a single character.
     */
    void moveBack() throws JsonReadException;

    /**
     * Returns the reference resolver registered for used by the reader.
     *
     * @return the reference resolver.
     */
    JsonMapReferenceResolver getReferenceResolver();

    /**
     * Returns the reader's identity retention flag.
     *
     * @return the reader's identity retention {@code boolean} flag.
     */
    boolean getIdentityRetainFlag();
}
