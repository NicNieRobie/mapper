package dev.vpendischuk.mapper.json.util;

/**
 * Common interface for date format validators -
 *   classes responsible for validation of date representation strings.
 */
public interface DateFormatValidator {
    /**
     * Checks if the specified {@code timeString} time representation
     *   is a valid {@link java.time.LocalTime} representation.
     *
     * @param timeString string that represents time.
     * @return {@code true} if string is a valid {@link java.time.LocalTime} representation.
     */
    boolean timeIsValid(String timeString);

    /**
     * Checks if the specified {@code dateString} date representation
     *   is a valid {@link java.time.LocalDate} representation.
     *
     * @param dateString string that represents date.
     * @return {@code true} if string is a valid {@link java.time.LocalDate} representation.
     */
    boolean dateIsValid(String dateString);

    /**
     * Checks if the specified {@code dateString} datetime representation
     *   is a valid {@link java.time.LocalDateTime} representation.
     *
     * @param dateTimeString string that represents datetime.
     * @return {@code true} if string is a valid {@link java.time.LocalDateTime} representation.
     */
    boolean dateTimeIsValid(String dateTimeString);
}
