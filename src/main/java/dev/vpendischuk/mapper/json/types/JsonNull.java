package dev.vpendischuk.mapper.json.types;

/**
 * Represents a null value in the JSON model.
 * <p>
 * Copies the behaviour of a JavaScript null value.
 */
public final class JsonNull extends JsonValue {
    /**
     * Returns a cloned value of the {@code JsonNull} instance.
     * @return the {@code JsonNull} value.
     */
    // Suppressing the warning as the clone behaviour is overridden.
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    protected Object clone() {
        return this;
    }

    /**
     * Checks if the {@code JsonNull} value is equal to the specified {@code object}.
     *
     * @param object the other object.
     * @return {@code true} if the {@code object} is null or a {@code JsonNull} instance.
     */
    // Suppressing the warning as the equals behaviour is overridden.
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object object) {
        return object == null || object == this;
    }

    /**
     * Returns the {@code JsonNull} instance hashcode.
     *
     * @return always 0.
     */
    @Override
    public int hashCode() {
        return 0;
    }

    /**
     * Converts the {@code JsonNull} instance to an instance of specified {@code objectClass} class.
     * <p>
     * <b>Note:</b> {@code JsonNull} values are always converted to null.
     *
     * @param objectClass class that the instance is converted to.
     * @param <T> converted value type.
     * @return converted value.
     */
    @Override
    public <T> Object toValue(Class<T> objectClass) {
        return null;
    }

    /**
     * Returns a JSON string representation of the {@code JsonNull} value.
     * <p>
     * <b>Note:</b> always returns "null".
     *
     * @return JSON string representation.
     */
    @Override
    public String toString() {
        return "null";
    }
}
