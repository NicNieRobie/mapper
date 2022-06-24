package dev.vpendischuk.mapper.json.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Class that wraps unit tests for the {@link DefaultDateFormatValidator} class.
 */
class DefaultDateFormatValidatorTest {
    /**
     * Provides arguments for the {@code timeStringIsValidCheckValid} test.
     *
     * @return arguments for the {@code timeStringIsValidCheckValid} test.
     */
    static Stream<Arguments> parameterizedValidTimeFormatTestData() {
        return Stream.of(
                Arguments.of("HH:mm:ss z", "15:13:08 EET"),
                Arguments.of("HHmmssZ", "165135+0200"),
                Arguments.of("h:mm a, z", "4:51 PM, EET")
        );
    }

    /**
     * Provides arguments for the {@code timeStringIsInvalidCheckInvalid} test.
     *
     * @return arguments for the {@code timeStringIsInvalidCheckInvalid} test.
     */
    static Stream<Arguments> parameterizedInvalidTimeFormatTestData() {
        return Stream.of(
                Arguments.of("HH:mm:ss z", "3:15 EET"),
                Arguments.of("HHmmssZ", "2012.02.07 4:51 PM, EET"),
                Arguments.of("h:mm a, z", "15:13:08 EET")
        );
    }

    /**
     * Provides arguments for the {@code dateStringIsValidCheckValid} test.
     *
     * @return arguments for the {@code dateStringIsValidCheckValid} test.
     */
    static Stream<Arguments> parameterizedValidDateFormatTestData() {
        return Stream.of(
                Arguments.of("yyyy.MM.dd", "2012.02.07"),
                Arguments.of("yyMMdd", "120314"),
                Arguments.of("d-MM-yyyy", "7-02-2004")
        );
    }

    /**
     * Provides arguments for the {@code dateStringIsInvalidCheckInvalid} test.
     *
     * @return arguments for the {@code dateStringIsInvalidCheckInvalid} test.
     */
    static Stream<Arguments> parameterizedInvalidDateFormatTestData() {
        return Stream.of(
                Arguments.of("yyyy.MM.dd", "120207"),
                Arguments.of("yyMMdd", "random"),
                Arguments.of("d-MM-yyyy", "7:02:2004")
        );
    }

    /**
     * Provides arguments for the {@code dateTimeStringIsValidCheckValid} test.
     *
     * @return arguments for the {@code dateTimeStringIsValidCheckValid} test.
     */
    static Stream<Arguments> parameterizedValidDateTimeFormatTestData() {
        return Stream.of(
                Arguments.of("yyyy.MM.dd 'at' HH:mm:ss z", "2012.02.07 at 15:13:08 EET"),
                Arguments.of("yyMMddHHmmssZ", "120207165135+0200"),
                Arguments.of("yyy.MM.dd h:mm a, z", "2012.02.07 4:51 PM, EET")
        );
    }

    /**
     * Provides arguments for the {@code dateTimeStringIsInvalidCheckInvalid} test.
     *
     * @return arguments for the {@code dateTimeStringIsInvalidCheckInvalid} test.
     */
    static Stream<Arguments> parameterizedInvalidDateTimeFormatTestData() {
        return Stream.of(
                Arguments.of("yyyy.MM.dd 'at' HH:mm:ss z", "03.25 PM"),
                Arguments.of("yyMMddHHmmssZ", "2012.02.07 4:51 PM, EET"),
                Arguments.of("yyy.MM.dd h:mm a, z", "2012.02.07 at 15:13:08 EET")
        );
    }

    /**
     * Checks if the {@code timeIsValid} method of the {@link DefaultDateFormatValidator} class
     *   returns {@code true} for valid {@link java.time.LocalTime}-representing strings
     *   for specified datetime format.
     *
     * @param dateTimeFormat datetime format string.
     * @param dateTimeString string to be validated.
     */
    @ParameterizedTest
    @MethodSource("parameterizedValidTimeFormatTestData")
    @DisplayName("Time string is considered valid if it fits the format")
    void timeStringIsValidCheckValid(String dateTimeFormat, String dateTimeString) {
        DefaultDateFormatValidator validator = new DefaultDateFormatValidator(dateTimeFormat);

        assertTrue(validator.timeIsValid(dateTimeString));
    }

    /**
     * Checks if the {@code timeIsValid} method of the {@link DefaultDateFormatValidator} class
     *   returns {@code false} for invalid {@link java.time.LocalTime}-representing strings
     *   for specified datetime format.
     *
     * @param dateTimeFormat datetime format string.
     * @param dateTimeString string to be validated.
     */
    @ParameterizedTest
    @MethodSource("parameterizedInvalidTimeFormatTestData")
    @DisplayName("Time string is considered invalid if it doesn't fit the format")
    void timeStringIsInvalidCheckInvalid(String dateTimeFormat, String dateTimeString) {
        DefaultDateFormatValidator validator = new DefaultDateFormatValidator(dateTimeFormat);

        assertFalse(validator.timeIsValid(dateTimeString));
    }

    /**
     * Checks if the {@code dateIsValid} method of the {@link DefaultDateFormatValidator} class
     *   returns {@code true} for valid {@link java.time.LocalDate}-representing strings
     *   for specified datetime format.
     *
     * @param dateTimeFormat datetime format string.
     * @param dateTimeString string to be validated.
     */
    @ParameterizedTest
    @MethodSource("parameterizedValidDateFormatTestData")
    @DisplayName("Date string is considered valid if it fits the format")
    void dateStringIsValidCheckValid(String dateTimeFormat, String dateTimeString) {
        DefaultDateFormatValidator validator = new DefaultDateFormatValidator(dateTimeFormat);

        assertTrue(validator.dateIsValid(dateTimeString));
    }

    /**
     * Checks if the {@code dateIsValid} method of the {@link DefaultDateFormatValidator} class
     *   returns {@code false} for invalid {@link java.time.LocalDate}-representing strings
     *   for specified datetime format.
     *
     * @param dateTimeFormat datetime format string.
     * @param dateTimeString string to be validated.
     */
    @ParameterizedTest
    @MethodSource("parameterizedInvalidDateFormatTestData")
    @DisplayName("Date string is considered invalid if it doesn't fit the format")
    void dateStringIsInvalidCheckInvalid(String dateTimeFormat, String dateTimeString) {
        DefaultDateFormatValidator validator = new DefaultDateFormatValidator(dateTimeFormat);

        assertFalse(validator.dateIsValid(dateTimeString));
    }

    /**
     * Checks if the {@code dateTimeIsValid} method of the {@link DefaultDateFormatValidator} class
     *   returns {@code true} for valid {@link java.time.LocalDateTime}-representing strings
     *   for specified datetime format.
     *
     * @param dateTimeFormat datetime format string.
     * @param dateTimeString string to be validated.
     */
    @ParameterizedTest
    @MethodSource("parameterizedValidDateTimeFormatTestData")
    @DisplayName("Datetime string is considered valid if it fits the format")
    void dateTimeStringIsValidCheckValid(String dateTimeFormat, String dateTimeString) {
        DefaultDateFormatValidator validator = new DefaultDateFormatValidator(dateTimeFormat);

        assertTrue(validator.dateTimeIsValid(dateTimeString));
    }

    /**
     * Checks if the {@code dateTimeIsValid} method of the {@link DefaultDateFormatValidator} class
     *   returns {@code false} for invalid {@link java.time.LocalDateTime}-representing strings
     *   for specified datetime format.
     *
     * @param dateTimeFormat datetime format string.
     * @param dateTimeString string to be validated.
     */
    @ParameterizedTest
    @MethodSource("parameterizedInvalidDateTimeFormatTestData")
    @DisplayName("Datetime string is considered invalid if it doesn't fit the format")
    void dateTimeStringIsInvalidCheckInvalid(String dateTimeFormat, String dateTimeString) {
        DefaultDateFormatValidator validator = new DefaultDateFormatValidator(dateTimeFormat);

        assertFalse(validator.dateTimeIsValid(dateTimeString));
    }
}