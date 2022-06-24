package dev.vpendischuk.mapper.json.exceptions;

/**
 * Exception that is raised in case if an object reference cycle
 *   has been detected when marshalling a JSON.
 */
public class CyclicReferenceException extends Error {
    /**
     * Initializes a new {@code CyclicReferenceException} instance
     *   with a specified message.
     *
     * @param message the message text.
     */
    public CyclicReferenceException(final String message) {
        super(message);
    }
}
