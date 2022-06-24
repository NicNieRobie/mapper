package dev.vpendischuk.mapper.json.exceptions;

/**
 * Exception that is raised in case if the date format provided in
 *   the {@code DateFormat} annotation is invalid.
 */
public class InvalidTimeFormatException extends Error {
    /**
     * Initializes a new {@code InvalidTimeFormatException} instance
     *   with a specified message.
     *
     * @param message the message text.
     */
    public InvalidTimeFormatException(final String message) {
        super(message);
    }

    /**
     * Initializes a new {@code InvalidTimeFormatException} instance
     *   with a specified message and a throwable that caused it.
     *
     * @param message the message text.
     * @param cause the throwable that caused the exception.
     */
    public InvalidTimeFormatException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
