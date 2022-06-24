package dev.vpendischuk.mapper.json.types;

import dev.vpendischuk.mapper.json.exceptions.JsonMappingException;

import java.util.Objects;

/**
 * Represents a boolean value in a JSON object model.
 */
public final class JsonBoolean extends JsonValue {
    // Boolean value represented by the instance.
    private final Boolean value;

    /**
     * Initializes a new {@code JsonBoolean} instance from specified value.
     *
     * @param value a {@code Boolean} value that is to be represented by the instance.
     */
    public JsonBoolean(Boolean value) {
        this.value = value;
    }

    /**
     * Tries to convert the {@code JsonBoolean} instance to specified {@code objectClass} class
     *   if this class represents a boolean value.
     *
     * @param objectClass class that the value is to be converted to.
     * @param <T> type of the value returned.
     * @return converted value.
     * @throws JsonMappingException if a boolean value cannot be mapped to given class.
     */
    @Override
    public <T> Object toValue(Class<T> objectClass) throws JsonMappingException {
        if (!(objectClass.equals(Boolean.class)) && !(objectClass.equals(boolean.class))) {
            throw new JsonMappingException("[JSON Number Error] " +
                    "Boolean value could not be mapped to class " + objectClass + ".");
        }

        return value;
    }

    /**
     * Returns a string representation of the boolean value in JSON format.
     * @return string representation of the boolean value.
     */
    @Override
    public String toString() {
        if (Objects.isNull(value)) {
            return "null";
        }

        return value.toString();
    }
}
