package dev.vpendischuk.mapper.json.types;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import dev.vpendischuk.mapper.json.exceptions.JsonMappingException;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Class that wraps unit tests for the {@link JsonString} class.
 */
class JsonStringTest {
    /**
     * Provides arguments for the {@code returnsContent} test.
     *
     * @return arguments for the {@code returnsContent} test.
     */
    static Stream<Arguments> contentRetrievalTestArgs() {
        return Stream.of(
                Arguments.of(new JsonString("string"), "string"),
                Arguments.of(new JsonString("string\nstring"), "string\\nstring"),
                Arguments.of(new JsonString("string\rs"), "string\\rs"),
                Arguments.of(new JsonString("string\ts"), "string\\ts"),
                Arguments.of(new JsonString('c'), "c")
        );
    }

    /**
     * Provides arguments for the {@code convertsToJsonString} test.
     *
     * @return arguments for the {@code convertsToJsonString} test.
     */
    static Stream<Arguments> toStringTestArgs() {
        return Stream.of(
                Arguments.of(new JsonString("string"), "\"string\""),
                Arguments.of(new JsonString("string\nstring"), "\"string\\nstring\""),
                Arguments.of(new JsonString("string\rs"), "\"string\\rs\""),
                Arguments.of(new JsonString("string\ts"), "\"string\\ts\""),
                Arguments.of(new JsonString('c'), "\"c\""),
                Arguments.of(new JsonString("\"quotes\""), "'\"quotes\"'")
        );
    }

    /**
     * Provides arguments for the {@code convertsToValue} test.
     *
     * @return arguments for the {@code convertsToValue} test.
     */
    static Stream<Arguments> valueConversionTestArgs() {
        return Stream.of(
                Arguments.of(new JsonString("string"), String.class, "string", false),
                Arguments.of(new JsonString("string\nstring"), String.class, "string\\nstring", false),
                Arguments.of(new JsonString("string\rs"), Character.class, 's', false),
                Arguments.of(new JsonString("string\ts"), Integer.class, null, true),
                Arguments.of(new JsonString('c'), char.class, 'c', false)
        );
    }

    /**
     * Tests if a {@link JsonString} instance returns its contents validly.
     *
     * @param jsonString the {@link JsonString} instance.
     * @param expectedContent expected string content.
     */
    @ParameterizedTest
    @MethodSource("contentRetrievalTestArgs")
    @DisplayName("Returns wrapped string or character content")
    void returnsContent(JsonString jsonString, String expectedContent) {
        assertEquals(expectedContent, jsonString.getContent());
    }

    /**
     * Tests if a {@link JsonString} instance is validly converted into a
     *   character\string type object and throws an exception if an attempt
     *   is made to convert it to a type that does not represent a string or a character.
     *
     * @param jsonString the {@link JsonString} instance.
     * @param conversionClass target class for conversion.
     * @param expectedValue expected value after conversion.
     * @param exceptionExpected flag that denotes if a {@link JsonMappingException} is expected to be thrown
     *                          as a result of a conversion attempt.
     * @param <T> type of the expected value after conversion.
     */
    @ParameterizedTest
    @MethodSource("valueConversionTestArgs")
    @DisplayName("Is correctly converted into a character or string value " +
            "and throws an exception if type is not String or char/Character")
    <T> void convertsToValue(JsonString jsonString, Class<T> conversionClass, T expectedValue, boolean exceptionExpected) {
        if (exceptionExpected) {
            assertThrows(JsonMappingException.class, () -> jsonString.toValue(conversionClass));
        } else {
            assertAll(
                    () -> assertDoesNotThrow(() -> jsonString.toValue(conversionClass)),
                    () -> assertEquals(expectedValue, jsonString.toValue(conversionClass))
            );
        }
    }

    /**
     * Tests if a {@link JsonString} instance is validly converted
     *   into its JSON string representation.
     *
     * @param jsonString the {@link JsonString} instance.
     * @param expected expected JSON string.
     */
    @ParameterizedTest
    @MethodSource("toStringTestArgs")
    @DisplayName("Is correctly converted into a JSON string value")
    void convertsToJsonString(JsonString jsonString, String expected) {
        assertEquals(expected, jsonString.toString());
    }
}