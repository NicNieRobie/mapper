package dev.vpendischuk.mapper.json.types;

import dev.vpendischuk.mapper.json.exceptions.JsonMappingException;

/**
 * Represents a string value in the JSON object model.
 */
public final class JsonString extends JsonValue {
    // String content.
    private final String content;

    /**
     * Initializes a new {@code JsonString} instance
     *   with specified {@code content} text.
     *
     * @param content JSON string value content.
     */
    public JsonString(String content) {
        // Replacing escape sequences with their implicit text codes
        // to avoid them when printing a JSON representation.
        this.content = String.valueOf(content)
                .replace("\n", "\\n")
                .replace("\t", "\\t")
                .replace("\f", "\\f")
                .replace("\b", "\\b")
                .replace("\r", "\\r");
    }

    /**
     * Initializes a new {@code JsonString} instance
     *   with specified {@code content} character.
     *
     * @param content JSON string value content as a character.
     */
    public JsonString(Character content) {
        // Replacing escape sequences with their implicit text codes
        // to avoid them when printing a JSON representation.
        this.content = String.valueOf(content)
                .replace("\n", "\\n")
                .replace("\t", "\\t")
                .replace("\f", "\\f")
                .replace("\b", "\\b")
                .replace("\r", "\\r");
    }

    /**
     * Returns the string value content of the {@code JsonString} value wrapper.
     *
     * @return string value content.
     */
    public String getContent() {
        return content;
    }

    /**
     * Returns the string value content of the {@code JsonString} value wrapper.
     *
     * @return string value content.
     */
    @Override
    public <T> Object toValue(Class<T> objectClass) throws JsonMappingException {
        if (String.class.isAssignableFrom(objectClass)) {
            return content;
        } else if (Character.class.isAssignableFrom(objectClass) || objectClass.equals(char.class)) {
            return content.charAt(0);
        } else {
            throw new JsonMappingException("[JSON String Error] " +
                    "Type mismatch: Text cannot be mapped to " + objectClass + " class.");
        }
    }

    /**
     * Returns the JSON string representation of the {@code JsonString} value.
     *
     * @return JSON string representation.
     */
    @Override
    public String toString() {
        if (content.equals("null")) {
            return content;
        }

        char quote = content.contains("\"") ? '\'' : '\"';

        return quote + content + quote;
    }
}
