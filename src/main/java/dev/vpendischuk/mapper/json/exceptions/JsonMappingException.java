package dev.vpendischuk.mapper.json.exceptions;

/**
 * Exception that is raised in case if the JSON could not be
 *   marshalled or unmarshalled for the given object.
 * <p>
 * May be caused by:
 * <ul>
 *     <li>invalid object type (not annotated as {@link dev.vpendischuk.mapper.json.annotations.Exported},
 *     no valid constructor for marshalled data, is a parameterized type etc.);</li>
 *     <li>property name specified in an annotation was equal to the name of another field;</li>
 *     <li>unmarshalling error (missing value when {@code unknownPropertiesPolicy}
 *     is set to {@code FAIL}, failure of object instantiation, type mismatch).</li>
 * </ul>
 */
public class JsonMappingException extends Error {
    /**
     * Initializes a new {@code JsonMappingException} instance
     *   with a specified message.
     *
     * @param message the message text.
     */
    public JsonMappingException(final String message) {
        super(message);
    }

    /**
     * Initializes a new {@code JsonMappingException} instance
     *   with a specified message and a throwable that caused it.
     *
     * @param message the message text.
     * @param cause the throwable that caused the exception.
     */
    public JsonMappingException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Initializes a new {@code JsonMappingException} instance
     *   with a throwable that caused it.
     *
     * @param cause the throwable that caused the exception.
     */
    public JsonMappingException(final Throwable cause) {
        super(cause.getMessage(), cause);
    }
}
