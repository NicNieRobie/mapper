package dev.vpendischuk.mapper.json.types;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import dev.vpendischuk.mapper.json.exceptions.JsonMappingException;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Class that wraps unit tests for the {@link JsonBoolean} class.
 */
class JsonNumberTest {
    /**
     * Provides arguments for the {@code convertsToNumericalValue} test.
     *
     * @return arguments for the {@code convertsToNumericalValue} test.
     */
    static Stream<Arguments> numericalValueConversionTestArgs() {
        return Stream.of(
                Arguments.of(new JsonNumber(2.5), double.class, false),
                Arguments.of(new JsonNumber(2.5), Integer.class, false),
                Arguments.of(new JsonNumber(2.5), short.class, false),
                Arguments.of(new JsonNumber(257L), Float.class, false),
                Arguments.of(new JsonNumber(36L), Byte.class, false),
                Arguments.of(new JsonNumber(75L), Long.class, false),
                Arguments.of(new JsonNumber(2.5), String.class, true)
        );
    }

    /**
     * Provides arguments for the {@code convertsToString} test.
     *
     * @return arguments for the {@code convertsToString} test.
     */
    static Stream<Arguments> stringConversionTestArgs() {
        return Stream.of(
                Arguments.of(new JsonNumber(2.5), "2.5"),
                Arguments.of(new JsonNumber(3L), "3"),
                Arguments.of(new JsonNumber(7L), "7"),
                Arguments.of(new JsonNumber(257L), "257")
        );
    }

    /**
     * Tests if a {@link JsonNumber} instance converts to numerical types correctly
     *   and throws an exception for non-numerical types.
     *
     * @param jsonNumber value that is to be converted.
     * @param valueType target type for conversion.
     * @param exceptionExpected flag that denotes if a {@link JsonMappingException} is expected
     *                          when trying to convert the value to type {@code valueType}.
     */
    @ParameterizedTest
    @MethodSource("numericalValueConversionTestArgs")
    @DisplayName("Converts itself into specified type if it represents a numerical type " +
            "and throws and exception otherwise")
    void convertsToNumericalValue(JsonNumber jsonNumber, Class<?> valueType, boolean exceptionExpected) {
        if (exceptionExpected) {
            assertThrows(JsonMappingException.class, () -> jsonNumber.toValue(valueType));
        } else {
            assertDoesNotThrow(() -> jsonNumber.toValue(valueType));
        }
    }

    /**
     * Tests if a {@link JsonNumber} instance converts to a JSON string value.
     *
     * @param jsonNumber value that is to be converted.
     * @param expectedOutput expected JSON string.
     */
    @ParameterizedTest
    @MethodSource("stringConversionTestArgs")
    @DisplayName("Is converted into a string correctly")
    void convertsToString(JsonNumber jsonNumber, String expectedOutput) {
        assertEquals(expectedOutput, jsonNumber.toString());
    }
}