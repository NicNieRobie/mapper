package dev.vpendischuk.mapper.json.exceptions;

/**
 * Exception that is raised if a syntax error was detected when reading
 *   JSON for unmarshalling.
 */
public class JsonReadException extends Error {
    /**
     * Initializes a new {@code JsonReadException} instance
     *   with a specified message.
     *
     * @param message the message text.
     */
    public JsonReadException(final String message) {
        super(message);
    }

    /**
     * Initializes a new {@code JsonReadException} instance
     *   with a specified message and a throwable that caused it.
     *
     * @param message the message text.
     * @param cause the throwable that caused the exception.
     */
    public JsonReadException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
