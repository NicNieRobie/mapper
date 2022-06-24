package dev.vpendischuk.mapper.json.types;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import dev.vpendischuk.mapper.json.annotations.Exported;
import dev.vpendischuk.mapper.json.annotations.enums.NullHandling;
import dev.vpendischuk.mapper.json.annotations.enums.UnknownPropertiesPolicy;
import dev.vpendischuk.mapper.json.exceptions.JsonMappingException;
import dev.vpendischuk.mapper.json.util.DefaultJsonMapReferenceResolver;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Class that wraps unit tests for the {@link JsonArray} class.
 */
class JsonArrayTest {
    /**
     * Class used as a collection element type in unit tests.
     */
    @Exported(nullHandling = NullHandling.INCLUDE, unknownPropertiesPolicy = UnknownPropertiesPolicy.IGNORE)
    record TestArrayElementClass(String name, int value, boolean boolValue) { }

    /**
     * Class used as a dummy type for invalid collection element type conversion tests.
     */
    @Exported
    record TestWrongArrayElementClass(String info) { }

    /**
     * Class used as a list field wrapper for field collection retrieval tests.
     */
    record GetFieldCollectionTestClass(List<TestArrayElementClass> list) { }

    /**
     * Tests if a {@link JsonArray} class instance can be initialized via a collection
     *   and construct its model without raising exceptions.
     */
    @Test
    @DisplayName("Successfully initializes a JSON model of specified collection")
    void constructsFromCollection() {
        List<TestArrayElementClass> list = new ArrayList<>();

        TestArrayElementClass element1 = new TestArrayElementClass("elem1", 15, true);
        TestArrayElementClass element2 = new TestArrayElementClass(null, 10, false);

        list.add(element1);
        list.add(element2);

        Assertions.assertAll(
                () -> assertDoesNotThrow(
                        () -> new JsonArray(list, new DefaultJsonMapReferenceResolver(), true, false)
                )
        );
    }

    /**
     * Tests if a {@link JsonArray} class instance is successfully converted
     *   into its JSON string representation.
     */
    @Test
    @DisplayName("Successfully converts to a JSON string")
    void convertsToString() {
        List<TestArrayElementClass> list = new ArrayList<>();

        TestArrayElementClass element1 = new TestArrayElementClass("elem1", 15, true);
        TestArrayElementClass element2 = new TestArrayElementClass(null, 10, false);

        list.add(element1);
        list.add(element2);

        JsonArray jsonArray = new JsonArray(list, new DefaultJsonMapReferenceResolver(), true, false);

        String testString = "[{\"boolValue\":true,\"name\":\"elem1\",\"value\":15}," +
                "{\"boolValue\":false,\"name\":null,\"value\":10}]";

        assertEquals(testString, jsonArray.toString());
    }

    /**
     * Tests if a {@link JsonArray} class instance is successfully converted
     *   into a collection it models.
     */
    @Test
    @DisplayName("Successfully converts to a collection of corresponding type")
    void convertsToCollection() {
        List<TestArrayElementClass> list = new ArrayList<>();

        TestArrayElementClass element1 = new TestArrayElementClass("elem1", 15, true);
        TestArrayElementClass element2 = new TestArrayElementClass(null, 10, false);

        list.add(element1);
        list.add(element2);

        JsonArray jsonArray = new JsonArray(list, new DefaultJsonMapReferenceResolver(), true, false);

        List<TestArrayElementClass> recoveredArray = new ArrayList<>(jsonArray.toValue(TestArrayElementClass.class));

        Assertions.assertAll(
                () -> assertEquals(element1, recoveredArray.toArray()[0]),
                () -> assertEquals(element2, recoveredArray.toArray()[1])
        );
    }

    /**
     * Tests if a {@link JsonArray} class instance throws a {@link JsonMappingException}
     *   when trying to convert into a collection of a wrongly typed elements.
     */
    @Test
    @DisplayName("Throws JsonMappingException on trying to convert collection of wrong type")
    void convertsToCollectionFail() {
        List<TestArrayElementClass> list = new ArrayList<>();

        TestArrayElementClass element1 = new TestArrayElementClass("elem1", 15, true);
        TestArrayElementClass element2 = new TestArrayElementClass(null, 10, false);

        list.add(element1);
        list.add(element2);

        JsonArray jsonArray = new JsonArray(list, new DefaultJsonMapReferenceResolver(), true, false);

        Assertions.assertThrows(JsonMappingException.class, () -> jsonArray.toValue(TestWrongArrayElementClass.class));
    }

    /**
     * Tests if a {@link JsonArray} class successfully converts into a collection of implicitly
     *   specified parameterized type.
     */
    @Test
    @DisplayName("Successfully converts to a collection of specified parameterized field type")
    void convertsToParameterizedCollection() {
        List<TestArrayElementClass> list = new ArrayList<>();

        TestArrayElementClass element1 = new TestArrayElementClass("elem1", 15, true);
        TestArrayElementClass element2 = new TestArrayElementClass(null, 10, false);

        list.add(element1);
        list.add(element2);

        JsonArray jsonArray = new JsonArray(list, new DefaultJsonMapReferenceResolver(), true, false);

        Class<?> getFieldCollectionTest = GetFieldCollectionTestClass.class;
        try {
            Field listField = getFieldCollectionTest.getDeclaredField("list");

            Collection<?> recoveredArray;

            recoveredArray = (Collection<?>) jsonArray.getCollectionFromValue(
                    listField.getClass(),
                    (ParameterizedType) listField.getGenericType()
            );

            Assertions.assertAll(
                    () -> assertEquals(element1, recoveredArray.toArray()[0]),
                    () -> assertEquals(element2, recoveredArray.toArray()[1])
            );
        } catch (NoSuchFieldException ex) {
            throw new IllegalArgumentException("No field named 'list' was found.");
        }
    }
}