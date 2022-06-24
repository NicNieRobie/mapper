package dev.vpendischuk.mapper.json.types;

import dev.vpendischuk.mapper.json.exceptions.JsonMappingException;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents a generic number value in a JSON object model.
 * <p>
 * <b>Warning:</b> cannot wrap a value that cannot be wrapped in a {@link BigDecimal} wrapper.
 */
public final class JsonNumber extends JsonValue {
    // Wrapped numerical value.
    private final BigDecimal value;

    /**
     * Initializes a new {@code JsonNumber} instance
     *   from specified string that represents a value.
     *
     * @param string string representation of a numeric value.
     */
    public JsonNumber(String string) {
        // Parsing the string to obtain the numeric value to wrap.
        if (string == null) {
            this.value = null;
        } else {
            this.value = new BigDecimal(string);
        }
    }

    /**
     * Initializes a new {@code JsonNumber} instance from
     *   specified {@code Double} representation of the value.
     *
     * @param value {@code Double} representation of a numerical value.
     */
    public JsonNumber(Double value) {
        if (value == null) {
            this.value = null;
        } else {
            this.value = BigDecimal.valueOf(value);
        }
    }

    /**
     * Initializes a new {@code JsonNumber} instance from
     *   specified {@code Double} representation of the value.
     *
     * @param value {@code Double} representation of a numerical value.
     */
    public JsonNumber(Long value) {
        if (value == null) {
            this.value = null;
        } else {
            this.value = BigDecimal.valueOf(value);
        }
    }

    /**
     * Tries to convert the numerical value wrapped in the {@code JsonNumber} instance
     *   to the specified {@code objectClass} numerical class.
     *
     * @param objectClass class that the value will be converted to;
     *                    must represent a {@link Number} wrapper or a numerical primitive.
     * @param <T> type of the converted value (inferred from the {@code objectClass}).
     * @return converted numerical value.
     * @throws JsonMappingException if the specified class cannot represent a number.
     */
    @Override
    public <T> Object toValue(Class<T> objectClass) throws JsonMappingException {
        if (Double.class.isAssignableFrom(objectClass) || objectClass.equals(double.class)) {
            return value.doubleValue();
        }
        if (Float.class.isAssignableFrom(objectClass) || objectClass.equals(float.class)) {
            return value.floatValue();
        }
        if (Long.class.isAssignableFrom(objectClass) || objectClass.equals(long.class)) {
            return value.longValue();
        }
        if (Integer.class.isAssignableFrom(objectClass) || objectClass.equals(int.class)) {
            return value.intValue();
        }
        if (Short.class.isAssignableFrom(objectClass) || objectClass.equals(short.class)) {
            return value.shortValue();
        }
        if (Byte.class.isAssignableFrom(objectClass) || objectClass.equals(byte.class)) {
            return value.byteValue();
        }

        throw new JsonMappingException("[JSON Number Error] " +
                "Value could not be mapped to class " + objectClass + ".");
    }

    /**
     * Returns a JSON string representation of the numerical value
     *   wrapped in the {@code JsonNumber} instance.
     * @return a JSON string representation of the numerical value.
     */
    @Override
    public String toString() {
        if (Objects.isNull(value)) {
            return "null";
        }

        return value.toString();
    }
}
