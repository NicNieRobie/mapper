package dev.vpendischuk.mapper.json.types;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import dev.vpendischuk.mapper.json.annotations.DateFormat;
import dev.vpendischuk.mapper.json.annotations.Exported;
import dev.vpendischuk.mapper.json.annotations.PropertyName;
import dev.vpendischuk.mapper.json.exceptions.JsonMappingException;
import dev.vpendischuk.mapper.json.util.DefaultJsonMapReferenceResolver;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Class that wraps unit tests for the {@link JsonObject} class.
 */
class JsonObjectTest {
    /**
     * Enum used as a test value type.
     */
    enum TestEnum {
        TEST_VAL
    }

    /**
     * Class that is not annotated with an {@link Exported} annotation
     *   used for tests.
     */
    static class UnannotatedClass { }

    /**
     * Generic class used for tests.
     *
     * @param <T> arbitrary type parameter.
     */
    @Exported
    static class GenericClass<T> {
        @SuppressWarnings("unused")
        T field;

        GenericClass() { }
    }

    /**
     * Class without a parameterless constructor used for tests.
     */
    @Exported
    static class ClassWithoutParameterlessConstructor {
        public ClassWithoutParameterlessConstructor(@SuppressWarnings("unused") String string) { }
    }

    /**
     * {@link Exported} record that is used in tests for marshalling and unmarshalling.
     */
    @Exported
    static record TestRecord(@PropertyName("nameVal") String name, Double value, List<String> list) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestRecord that = (TestRecord) o;
            return Objects.equals(name, that.name) && Objects.equals(value, that.value) &&
                    Objects.equals(list, that.list);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, value, list);
        }
    }

    /**
     * {@link Exported} class that is used in tests for marshalling and unmarshalling.
     */
    @Exported
    static class TestClass {
        TestEnum enumValue;
        boolean boolValue;
        @DateFormat("dd-MM:yyyy")
        LocalDate date;

        @SuppressWarnings("unused")
        public TestClass() { }

        public TestClass(TestEnum enumValue, boolean boolValue, LocalDate date) {
            this.enumValue = enumValue;
            this.boolValue = boolValue;
            this.date = date;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestClass testClass = (TestClass) o;
            return boolValue == testClass.boolValue && enumValue == testClass.enumValue &&
                    Objects.equals(date, testClass.date);
        }

        @Override
        public int hashCode() {
            return Objects.hash(enumValue, boolValue, date);
        }
    }

    /**
     * Provides arguments for the {@code doesNotSupportInvalidTypes} test.
     *
     * @return arguments for the {@code doesNotSupportInvalidTypes} test.
     */
    static Stream<Arguments> unsupportedTypesTestArgs() {
        return Stream.of(
                Arguments.of(new UnannotatedClass()),
                Arguments.of(new GenericClass<>()),
                Arguments.of(new ClassWithoutParameterlessConstructor("text"))
        );
    }

    /**
     * Provides arguments for the {@code constructsModelFromSupportedTypeObject} and {@code convertsModelToValue} test.
     *
     * @return arguments for the {@code constructsModelFromSupportedTypeObject} and {@code convertsModelToValue} test.
     */
    static Stream<Arguments> supportedTypesTestArgs() {
        return Stream.of(
                Arguments.of(new TestRecord("name1", 2.4, Arrays.asList("str1", "str2"))),
                Arguments.of(new TestRecord("name2", 3.6, Arrays.asList("str3", "str4"))),
                Arguments.of(new TestClass(TestEnum.TEST_VAL, false,
                        LocalDate.of(2003, 11, 4)))
        );
    }

    /**
     * Provides arguments for the {@code convertsToJsonString} test.
     *
     * @return arguments for the {@code convertsToJsonString} test.
     */
    static Stream<Arguments> stringConversionTestArgs() {
        return Stream.of(
                Arguments.of(new TestRecord("name1", 2.4, Arrays.asList("str1", "str2")),
                        "{\"list\":[\"str1\",\"str2\"],\"nameVal\":\"name1\",\"value\":2.4}"),
                Arguments.of(new TestRecord("name2", 3.6, Arrays.asList("str3", "str4")),
                        "{\"list\":[\"str3\",\"str4\"],\"nameVal\":\"name2\",\"value\":3.6}"),
                Arguments.of(new TestClass(TestEnum.TEST_VAL, false, LocalDate.of(2003, 11, 4)),
                        "{\"boolValue\":false,\"date\":\"04-11:2003\",\"enumValue\":\"TEST_VAL\"}")
        );
    }

    /**
     * Provides arguments for the {@code keyValuePairAdditionTest} test.
     *
     * @return arguments for the {@code keyValuePairAdditionTest} test.
     */
    static Stream<Arguments> keyValuePairAdditionTest() {
        return Stream.of(
                Arguments.of("obj", new TestRecord("name1", 2.4, Arrays.asList("str1", "str2")),
                        "{\"obj\":{\"list\":[\"str1\",\"str2\"],\"nameVal\":\"name1\",\"value\":2.4}}"),
                Arguments.of("obj", new TestClass(TestEnum.TEST_VAL, false, LocalDate.of(2003, 11, 4)),
                        "{\"obj\":{\"boolValue\":false,\"date\":\"04-11:2003\",\"enumValue\":\"TEST_VAL\"}}"),
                Arguments.of("str", "string", "{\"str\":\"string\"}"),
                Arguments.of("var", 6.3, "{\"var\":6.3}"),
                Arguments.of("bool", true, "{\"bool\":true}")
        );
    }

    /**
     * Tests if the {@link JsonObject} constructor throws a {@link JsonMappingException} when
     *   an attempt is made to initialize an instance from an unsupported type.
     *
     * @param unsupportedObj instance of an unsupported type.
     */
    @ParameterizedTest
    @MethodSource("unsupportedTypesTestArgs")
    @DisplayName("Throws an exception when trying to marshall an unsupported class object")
    void doesNotSupportInvalidTypes(Object unsupportedObj) {
        assertThrows(JsonMappingException.class, () -> new JsonObject(unsupportedObj, false));
    }

    /**
     * Tests if a {@link JsonObject} instance is successfully initialized as a model
     *   of an object {@code obj} if an object is of a supported type.
     *
     * @param obj object of a supported type.
     */
    @ParameterizedTest
    @MethodSource("supportedTypesTestArgs")
    @DisplayName("Correctly constructs a model of a valid object without raising exceptions")
    void constructsModelFromSupportedTypeObject(Object obj) {
        assertDoesNotThrow(() -> new JsonObject(obj, false));
    }

    /**
     * Tests if a {@link JsonObject} instance adds a valid JSON model representation
     *   of the specified key-value pair.
     *
     * @param keyValue key value.
     * @param value pair value.
     * @param expectedJson expected JSON representation.
     */
    @ParameterizedTest
    @MethodSource("keyValuePairAdditionTest")
    @DisplayName("Adds a valid JSON representation of a kay-value pair in the model")
    void addsKeyValuePair(String keyValue, Object value, String expectedJson) {
        JsonObject jsonObject = new JsonObject(new DefaultJsonMapReferenceResolver(), false);

        assertAll(
                () -> assertDoesNotThrow(() -> jsonObject.addKeyValuePair(keyValue, value)),
                () -> assertEquals(expectedJson, jsonObject.toString())
        );
    }

    /**
     * Tests if a {@link JsonObject} instance is converted to a valid JSON string value.
     *
     * @param obj object to be marshalled.
     * @param expectedValue expected JSON string.
     */
    @ParameterizedTest
    @MethodSource("stringConversionTestArgs")
    @DisplayName("Correctly converts to JSON string representation of an object")
    void convertsToJsonString(Object obj, String expectedValue) {
        assertEquals(expectedValue, new JsonObject(obj, false).toString());
    }

    /**
     * Checks if a {@link JsonObject} instance is correctly converted to a value of valid type.
     *
     * @param obj object to be marshalled.
     */
    @ParameterizedTest
    @MethodSource("supportedTypesTestArgs")
    @DisplayName("Correctly converts model to an object restored from its JSON model")
    void convertsModelToValue(Object obj) {
        JsonObject jsonObject = new JsonObject(obj, false);

        Object restored = jsonObject.toValue(obj.getClass());

        assertEquals(obj, restored);
    }
}