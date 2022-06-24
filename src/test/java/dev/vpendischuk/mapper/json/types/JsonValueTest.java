package dev.vpendischuk.mapper.json.types;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Class that wraps unit tests for the {@link JsonValue} class.
 */
class JsonValueTest {
    /**
     * Provides arguments for the {@code checksIfJsonString} test.
     *
     * @return arguments for the {@code checksIfJsonString} test.
     */
    static Stream<Arguments> jsonStringCheckTestArgs() {
        return Stream.of(
                Arguments.of(new JsonString("value"), true),
                Arguments.of(new JsonString("ssss"), true),
                Arguments.of(new JsonBoolean(false), false)
        );
    }

    /**
     * Provides arguments for the {@code convertsToPrimitiveJsonValue} test.
     *
     * @return arguments for the {@code convertsToPrimitiveJsonValue} test.
     */
    static Stream<Arguments> stringToPrimitiveValueTestArgs() {
        return Stream.of(
                Arguments.of("true", JsonBoolean.class),
                Arguments.of("\"string\"", JsonString.class),
                Arguments.of("2.75", JsonNumber.class),
                Arguments.of("null", JsonNull.class)
        );
    }

    /**
     * Provides arguments for the {@code parseDateTimeJsonValue} test.
     *
     * @return arguments for the {@code parseDateTimeJsonValue} test.
     */
    static Stream<Arguments> datetimeConversionTestArgs() {
        return Stream.of(
                Arguments.of(new JsonString("21.05.2021"), LocalDate.class,
                        "dd.MM.yyyy", LocalDate.of(2021, 5, 21)),
                Arguments.of(new JsonString("2021-05-21"), LocalDate.class,
                        null, LocalDate.of(2021, 5, 21)),
                Arguments.of(new JsonString("21.05.2021 03:11"), LocalDateTime.class,
                        "dd.MM.yyyy HH:mm", LocalDateTime.of(2021, 5, 21, 3, 11)),
                Arguments.of(new JsonString("2021-05-21T03:11"), LocalDateTime.class,
                        null, LocalDateTime.of(2021, 5, 21, 3, 11)),
                Arguments.of(new JsonString("03-11-26"), LocalTime.class,
                        "HH-mm-ss", LocalTime.of(3, 11, 26)),
                Arguments.of(new JsonString("03:11:26"), LocalTime.class,
                        null, LocalTime.of(3, 11, 26)),
                Arguments.of(new JsonNumber("3"), LocalTime.class, "H",  LocalTime.of(3, 0))
        );
    }

    /**
     * Tests if a {@link JsonValue} instance is correctly checked
     *   for being a {@link JsonString} instance.
     *
     * @param jsonValue the {@link JsonValue} instance.
     * @param isJsonString flag that denotes if {@code jsonValue} is a JSON string.
     */
    @ParameterizedTest
    @MethodSource("jsonStringCheckTestArgs")
    @DisplayName("Correctly checks if the JSON value is a JSON string")
    void checksIfJsonString(JsonValue jsonValue, boolean isJsonString) {
        assertEquals(isJsonString, jsonValue.isJsonString());
    }

    /**
     * Tests if a string that represents a primitive JSON value
     *   is correctly converted to its {@link JsonValue} wrapper.
     *
     * @param valueString string that represents a value.
     * @param expectedJsonClass expected {@link JsonValue} subclass type of the converted value.
     */
    @ParameterizedTest
    @MethodSource("stringToPrimitiveValueTestArgs")
    @DisplayName("Correctly converts string values into primitive JSON values.")
    void convertsToPrimitiveJsonValue(String valueString, Class<?> expectedJsonClass) {
        JsonValue convertedValue = JsonValue.stringToPrimitiveJsonValue(valueString);

        assertEquals(expectedJsonClass, convertedValue.getClass());
    }

    /**
     * Tests if a {@link JsonValue} instance that represents a datetime value is correctly
     *   parsed and converted into a datetime value of specified {@code datetimeClass} type
     *   for the provided {@code dateFormat} date frmat string.
     *
     * @param value value to be converted.
     * @param datetimeClass target class for the conversion.
     * @param dateFormat date format string.
     * @param expectedValue expected datetime value - the result of the conversion.
     */
    @ParameterizedTest
    @MethodSource("datetimeConversionTestArgs")
    @DisplayName("Correctly parses the datetime JsonValue using the specified date format if possible")
    void parseDateTimeJsonValue(JsonValue value, Class<?> datetimeClass, String dateFormat, Object expectedValue) {
        Object convertedDateValue = JsonValue.parseDateTimeJsonValue(value, datetimeClass, dateFormat);

        assertEquals(expectedValue, convertedDateValue);
    }
}