package dev.vpendischuk.mapper.json.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Default implementation of {@link DateFormatValidator} interface
 *   used by {@link dev.vpendischuk.mapper.json.types.JsonObject} instances
 *   for validating the date string values for fields annotated
 *   with {@link dev.vpendischuk.mapper.json.annotations.DateFormat}.
 */
public class DefaultDateFormatValidator implements DateFormatValidator {
    // Formatter used to store the date format string for parsing attempts.
    private final DateTimeFormatter dateTimeFormatter;

    /**
     * Initializes a new {@code DefaultDateFormatValidator} instance
     *   for specified {@code dateFormat} date format string.
     *
     * @param dateFormat format of the datetime strings expected for validation.
     */
    public DefaultDateFormatValidator(String dateFormat) {
        this.dateTimeFormatter = DateTimeFormatter.ofPattern(dateFormat);
    }

    /**
     * Checks if the specified {@code timeString} time representation
     *   is a valid {@link java.time.LocalTime} representation.
     *
     * @param timeString string that represents time.
     * @return {@code true} if string is a valid {@link java.time.LocalTime} representation.
     */
    @Override
    public boolean timeIsValid(String timeString) {
        try {
            LocalTime.parse(timeString, this.dateTimeFormatter);
        } catch (DateTimeParseException e) {
            return false;
        }
        return true;
    }

    /**
     * Checks if the specified {@code dateString} date representation
     *   is a valid {@link java.time.LocalDate} representation.
     *
     * @param dateString string that represents date.
     * @return {@code true} if string is a valid {@link java.time.LocalDate} representation.
     */
    @Override
    public boolean dateIsValid(String dateString) {
        try {
            LocalDate.parse(dateString, this.dateTimeFormatter);
        } catch (DateTimeParseException e) {
            return false;
        }
        return true;
    }

    /**
     * Checks if the specified {@code dateString} datetime representation
     *   is a valid {@link java.time.LocalDateTime} representation.
     *
     * @param dateTimeString string that represents datetime.
     * @return {@code true} if string is a valid {@link java.time.LocalDateTime} representation.
     */
    @Override
    public boolean dateTimeIsValid(String dateTimeString) {
        try {
            LocalDateTime.parse(dateTimeString, this.dateTimeFormatter);
        } catch (DateTimeParseException e) {
            return false;
        }
        return true;
    }
}
