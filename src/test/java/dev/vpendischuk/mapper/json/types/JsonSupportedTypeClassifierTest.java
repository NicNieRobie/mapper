package dev.vpendischuk.mapper.json.types;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import dev.vpendischuk.mapper.json.annotations.Exported;
import dev.vpendischuk.mapper.json.types.enums.SupportedType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Class that wraps unit tests for the {@link JsonSupportedTypeClassifier} class.
 */
class JsonSupportedTypeClassifierTest {
    /**
     * Class that is used as an arbitrary example of a type that is unsupported
     *   by the {@link dev.vpendischuk.mapper.json.JsonMapper}.
     */
    static class UnsupportedTestClass { }

    /**
     * Class that is used as an arbitrary example of a type that is supported
     *   by the {@link dev.vpendischuk.mapper.json.JsonMapper}.
     */
    @Exported
    static class ExportedTestClass { }

    /**
     * Enum that is used as an arbitrary enum example.
     */
    enum TestEnum {
        TEST_VALUE
    }

    /**
     * Provides arguments for the {@code classifiesTypeSupport} test.
     *
     * @return arguments for the {@code classifiesTypeSupport} test.
     */
    static Stream<Arguments> typeSupportClassificationTestArgs() {
        return Stream.of(
                Arguments.of(5, SupportedType.NUMBER),
                Arguments.of(5.3, SupportedType.NUMBER),
                Arguments.of('c', SupportedType.CHARACTER),
                Arguments.of("string", SupportedType.STRING),
                Arguments.of("test", SupportedType.STRING),
                Arguments.of(true, SupportedType.BOOLEAN),
                Arguments.of(false, SupportedType.BOOLEAN),
                Arguments.of(TestEnum.TEST_VALUE, SupportedType.ENUM),
                Arguments.of(new ArrayList<>(), SupportedType.LIST),
                Arguments.of(new HashSet<>(), SupportedType.SET),
                Arguments.of(new ExportedTestClass(), SupportedType.EXPORTED),
                Arguments.of(LocalTime.of(5, 30), SupportedType.TIME),
                Arguments.of(LocalDate.of(2003, 12, 1), SupportedType.DATE),
                Arguments.of(LocalDateTime.of(2003, 12, 1, 5, 30),
                        SupportedType.DATETIME),
                Arguments.of(new UnsupportedTestClass(), SupportedType.NOT_SUPPORTED)
        );
    }

    /**
     * Tests if the {@code classifyType} method of the {@link JsonSupportedTypeClassifier} class
     *   classified the type of the {@code value} object correctly for the {@link dev.vpendischuk.mapper.json.JsonMapper}.
     *
     * @param value value the type of which is to be classified.
     * @param expectedClass expected classification.
     */
    @ParameterizedTest
    @MethodSource("typeSupportClassificationTestArgs")
    @DisplayName("Classifies types for JSON mapper correctly")
    void classifiesTypeSupport(Object value, SupportedType expectedClass) {
        Class<?> objectClass = value.getClass();

        assertEquals(JsonSupportedTypeClassifier.classifyType(objectClass), expectedClass);
    }
}