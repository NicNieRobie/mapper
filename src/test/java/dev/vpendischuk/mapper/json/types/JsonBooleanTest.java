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
class JsonBooleanTest {
    /**
     * Provides arguments for the {@code valueConversionOperatesCorrectly} test.
     *
     * @return arguments for the {@code valueConversionOperatesCorrectly} test.
     */
    static Stream<Arguments> valueConversionTestArgs() {
        return Stream.of(
                Arguments.of(Boolean.class, false),
                Arguments.of(boolean.class, false),
                Arguments.of(String.class, true),
                Arguments.of(Integer.class, true)
        );
    }

    /**
     * Provides arguments for the {@code convertsToString} test.
     *
     * @return arguments for the {@code convertsToString} test.
     */
    static Stream<Arguments> stringConversionTestArgs() {
        return Stream.of(
                Arguments.of(new JsonBoolean(true), "true"),
                Arguments.of(new JsonBoolean(false), "false"),
                Arguments.of(new JsonBoolean(null), "null")
        );
    }

    /**
     * Tests if a {@link JsonBoolean} instance converts to boolean types correctly
     *   and throws an exception for non-boolean types.
     *
     * @param conversionTargetClass target type for conversion.
     * @param exceptionExpected flag that denotes if an exception is expected
     *                          when trying to convert to type {@code conversionTargetClass}.
     */
    @ParameterizedTest
    @MethodSource("valueConversionTestArgs")
    @DisplayName("Converts to a boolean/Boolean value correctly and throws an exception for other types")
    void valueConversionOperatesCorrectly(Class<?> conversionTargetClass, boolean exceptionExpected) {
        JsonBoolean jsonBoolean = new JsonBoolean(true);

        if (exceptionExpected) {
            assertThrows(JsonMappingException.class, () -> jsonBoolean.toValue(conversionTargetClass));
        } else {
            assertDoesNotThrow(() -> jsonBoolean.toValue(conversionTargetClass));
        }
    }

    /**
     * Tests if a {@link JsonBoolean} instance converts to a JSON string value.
     *
     * @param jsonBoolean value that is to be converted.
     * @param expectedString expected output after conversion.
     */
    @ParameterizedTest
    @MethodSource("stringConversionTestArgs")
    @DisplayName("Converts to JSON string correctly")
    void convertsToString(JsonBoolean jsonBoolean, String expectedString) {
        assertEquals(expectedString, jsonBoolean.toString());
    }
}