package dev.vpendischuk.mapper.json;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import dev.vpendischuk.mapper.json.annotations.Exported;
import dev.vpendischuk.mapper.json.annotations.PropertyName;
import dev.vpendischuk.mapper.json.annotations.enums.NullHandling;
import dev.vpendischuk.mapper.json.annotations.enums.UnknownPropertiesPolicy;
import dev.vpendischuk.mapper.json.exceptions.JsonMappingException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Class that wraps unit tests for the {@link JsonMapper} class.
 */
class JsonMapperTest {
    /**
     * Class that is used as a test type for identity retention tests.
     */
    @Exported
    static class RefEqualityTestClass {
        public String name;

        @SuppressWarnings("unused")
        public RefEqualityTestClass() { }

        public RefEqualityTestClass(String name) {
            this.name = name;
        }
    }

    /**
     * Class that is used as a test type for marshalling and unmarshalling tests.
     */
    @Exported
    static class MapperTestClass {
        @PropertyName("ref0")
        private RefEqualityTestClass ref1;
        private RefEqualityTestClass ref2;

        public MapperTestClass() { }

        public void setRef1(RefEqualityTestClass ref1) {
            this.ref1 = ref1;
        }

        public void setRef2(RefEqualityTestClass ref2) {
            this.ref2 = ref2;
        }

        public RefEqualityTestClass getRef1() {
            return ref1;
        }

        public RefEqualityTestClass getRef2() {
            return ref2;
        }
    }

    /**
     * Class used for marshalling/unmarshalling tests with nulls included and unknowns ignored.
     */
    @Exported(nullHandling = NullHandling.INCLUDE, unknownPropertiesPolicy = UnknownPropertiesPolicy.IGNORE)
    static record NullIncludeUnknownIgnoreRecord(String nullVal, String nonNull) {
        public NullIncludeUnknownIgnoreRecord() {
            this(null, "not null");
        }
    }

    /**
     * Class used for marshalling/unmarshalling tests with nulls excluded and unknowns ignored.
     */
    @Exported(nullHandling = NullHandling.EXCLUDE, unknownPropertiesPolicy = UnknownPropertiesPolicy.IGNORE)
    static record NullExcludeUnknownIgnoreRecord(String nullVal, String nonNull) {
        public NullExcludeUnknownIgnoreRecord() {
            this(null, "not null");
        }
    }

    /**
     * Class used for marshalling/unmarshalling tests with nulls included and fail on unknown value encounter.
     */
    @Exported(nullHandling = NullHandling.INCLUDE, unknownPropertiesPolicy = UnknownPropertiesPolicy.FAIL)
    static record NullIncludeUnknownFailRecord(String nullVal, String nonNull) {
        public NullIncludeUnknownFailRecord() {
            this(null, "not null");
        }
    }

    /**
     * Class used for marshalling/unmarshalling tests with nulls excluded and fail on unknown value encounter.
     */
    @Exported(nullHandling = NullHandling.EXCLUDE, unknownPropertiesPolicy = UnknownPropertiesPolicy.FAIL)
    static record NullExcludeUnknownFailRecord(String nullVal, String nonNull) {
        public NullExcludeUnknownFailRecord() {
            this(null, "not null");
        }
    }

    /**
     * Tests if the {@link JsonMapper} class instance is able to correctly
     *   unmarshal an object from its string JSON representation via
     *   the {@code readFromString} method without identity retention.
     */
    @Test
    @DisplayName("Reads object data from string without identity retention")
    void readsFromStringWithoutIdentityRetention() {
        JsonMapper jsonMapper = new JsonMapper(false);

        String jsonString = "{\"ref0\":{\"name\":\"ref1\"},\"ref2\":{\"name\":\"ref2\"}}";

        Assertions.assertAll(
                () -> assertDoesNotThrow(() -> jsonMapper.readFromString(MapperTestClass.class, jsonString)),
                () -> assertEquals(jsonMapper.readFromString(MapperTestClass.class, jsonString).getClass(),
                        MapperTestClass.class),
                () -> assertEquals(jsonMapper.readFromString(MapperTestClass.class, jsonString).getRef1().name,
                        "ref1"),
                () -> assertEquals(jsonMapper.readFromString(MapperTestClass.class, jsonString).getRef2().name,
                        "ref2"),
                () -> {
                    MapperTestClass readObj = jsonMapper.readFromString(MapperTestClass.class, jsonString);
                    assertNotSame(readObj.ref1, readObj.ref2);
                }
        );
    }

    /**
     * Tests if the {@link JsonMapper} class instance is able to correctly
     *   unmarshal an object from its string JSON representation via
     *   the {@code readFromString} method with identity retention.
     */
    @Test
    @DisplayName("Reads object data from string with identity retention")
    void readsFromStringWithIdentityRetention() {
        JsonMapper jsonMapper = new JsonMapper(true);

        String jsonString = "{\"ref0\":{\"name\":\"ref\",\"$ref\":\"47fc9840-fe47-40cb-a47d-c45c58135c2b\"}," +
                "\"ref2\":{\"name\":\"ref\",\"$ref\":\"47fc9840-fe47-40cb-a47d-c45c58135c2b\"}}";

        Assertions.assertAll(
                () -> assertDoesNotThrow(() -> jsonMapper.readFromString(MapperTestClass.class, jsonString)),
                () -> assertEquals(jsonMapper.readFromString(MapperTestClass.class, jsonString).getClass(),
                        MapperTestClass.class),
                () -> assertEquals(jsonMapper.readFromString(MapperTestClass.class, jsonString).getRef1().name,
                        "ref"),
                () -> assertEquals(jsonMapper.readFromString(MapperTestClass.class, jsonString).getRef2().name,
                        "ref"),
                () -> {
                    MapperTestClass readObj = jsonMapper.readFromString(MapperTestClass.class, jsonString);
                    assertSame(readObj.ref1, readObj.ref2);
                }
        );
    }

    /**
     * Tests if the {@link JsonMapper} class instance is able to correctly
     *   unmarshal an object from its JSON representation in an input stream via
     *   the {@code read} method without identity retention.
     */
    @Test
    @DisplayName("Reads object data from stream without identity retention")
    void readsFromStreamWithoutIdentityRetention() {
        JsonMapper jsonMapper = new JsonMapper(true);

        String jsonString = "{\"ref0\":{\"name\":\"ref1\"},\"ref2\":{\"name\":\"ref2\"}}";

        Assertions.assertAll(
                () -> assertDoesNotThrow(() -> jsonMapper.read(MapperTestClass.class,
                        new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8)))),
                () -> assertEquals(
                        jsonMapper.read(MapperTestClass.class,
                                new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8))).getClass(),
                        MapperTestClass.class
                ),
                () -> assertEquals(jsonMapper.read(
                                MapperTestClass.class,
                                new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8))).getRef1().name,
                        "ref1"
                ),
                () -> assertEquals(
                        jsonMapper.read(MapperTestClass.class,
                                new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8))).getRef2().name,
                        "ref2"
                ),
                () -> {
                    MapperTestClass readObj = jsonMapper.read(MapperTestClass.class,
                            new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8)));
                    assertNotSame(readObj.ref1, readObj.ref2);
                }
        );
    }

    /**
     * Tests if the {@link JsonMapper} class instance is able to correctly
     *   unmarshal an object from its JSON representation in an input stream via
     *   the {@code read} method with identity retention.
     */
    @Test
    @DisplayName("Reads object data from stream with identity retention")
    void readsFromStreamWithIdentityRetention() {
        JsonMapper jsonMapper = new JsonMapper(true);

        String jsonString = "{\"ref0\":{\"name\":\"ref\",\"$ref\":\"47fc9840-fe47-40cb-a47d-c45c58135c2b\"}," +
                "\"ref2\":{\"name\":\"ref\",\"$ref\":\"47fc9840-fe47-40cb-a47d-c45c58135c2b\"}}";

        Assertions.assertAll(
                () -> assertDoesNotThrow(() -> jsonMapper.read(MapperTestClass.class,
                        new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8)))),
                () -> assertEquals(
                        jsonMapper.read(MapperTestClass.class,
                                new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8))).getClass(),
                        MapperTestClass.class
                ),
                () -> assertEquals(jsonMapper.read(
                        MapperTestClass.class,
                                new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8))).getRef1().name,
                        "ref"
                ),
                () -> assertEquals(
                        jsonMapper.read(MapperTestClass.class,
                                new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8))).getRef2().name,
                        "ref"
                ),
                () -> {
                    MapperTestClass readObj = jsonMapper.read(MapperTestClass.class,
                            new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8)));
                    assertSame(readObj.ref1, readObj.ref2);
                }
        );
    }

    /**
     * Tests if the {@link JsonMapper} class instance is able to correctly
     *   unmarshal an object from its JSON representation in a file via
     *   the {@code read} method without identity retention.
     */
    @Test
    @DisplayName("Reads object data from file without identity retention")
    void readsFromFileWithoutIdentityRetention() {
        JsonMapper jsonMapper = new JsonMapper(false);

        URL fileToRead = JsonMapperTest.class.getResource("testJson.json");

        if (fileToRead == null) {
            throw new IllegalStateException("File testJson not found.");
        }

        try {
            File jsonFile = new File(fileToRead.toURI());

            Assertions.assertAll(
                    () -> assertDoesNotThrow(() -> jsonMapper.read(MapperTestClass.class, jsonFile)),
                    () -> assertEquals(
                            jsonMapper.read(MapperTestClass.class, jsonFile).getClass(),
                            MapperTestClass.class
                    ),
                    () -> assertEquals(jsonMapper.read(MapperTestClass.class, jsonFile).getRef1().name,"ref1"),
                    () -> assertEquals(
                            jsonMapper.read(MapperTestClass.class, jsonFile).getRef2().name, "ref2"),
                    () -> {
                        MapperTestClass readObj = jsonMapper.read(MapperTestClass.class, jsonFile);
                        assertNotSame(readObj.ref1, readObj.ref2);
                    }
            );
        } catch (URISyntaxException ex) {
            throw new IllegalStateException("File testJson not found.", ex);
        }
    }

    /**
     * Tests if the {@link JsonMapper} class instance is able to correctly
     *   unmarshal an object from its JSON representation in a file via
     *   the {@code read} method with identity retention.
     */
    @Test
    @DisplayName("Reads object data from file with identity retention")
    void readsFromFileWithIdentityRetention() {
        JsonMapper jsonMapper = new JsonMapper(true);

        URL fileToRead = JsonMapperTest.class.getResource("testIdentityJson.json");

        if (fileToRead == null) {
            throw new IllegalStateException("File testJson not found.");
        }

        try {
            File jsonFile = new File(fileToRead.toURI());

            Assertions.assertAll(
                    () -> assertDoesNotThrow(() -> jsonMapper.read(MapperTestClass.class, jsonFile)),
                    () -> assertEquals(
                            jsonMapper.read(MapperTestClass.class, jsonFile).getClass(),
                            MapperTestClass.class
                    ),
                    () -> assertEquals(jsonMapper.read(MapperTestClass.class, jsonFile).getRef1().name,"ref"),
                    () -> assertEquals(
                            jsonMapper.read(MapperTestClass.class, jsonFile).getRef2().name, "ref"),
                    () -> {
                        MapperTestClass readObj = jsonMapper.read(MapperTestClass.class, jsonFile);
                        assertSame(readObj.ref1, readObj.ref2);
                    }
            );
        } catch (URISyntaxException ex) {
            throw new IllegalStateException("File testIdentityJson not found.", ex);
        }
    }

    /**
     * Tests if a {@link JsonMapper} class instance is able to marshall
     *   an object to a valid JSON format string representation and write
     *   it to a string without identity retention.
     */
    @Test
    @DisplayName("Writes an object to string as a JSON without identity retention")
    void writesObjectToJsonStringWithoutIdentityRetention() {
        JsonMapper jsonMapper = new JsonMapper(false);

        MapperTestClass obj = new MapperTestClass();
        RefEqualityTestClass ref1 = new RefEqualityTestClass("ref1");
        RefEqualityTestClass ref2 = new RefEqualityTestClass("ref2");

        obj.setRef1(ref1);
        obj.setRef2(ref2);

        String jsonString = jsonMapper.writeToString(obj);
        String testString = "{\"ref0\":{\"name\":\"ref1\"},\"ref2\":{\"name\":\"ref2\"}}";

        Assertions.assertEquals(testString, jsonString);
    }

    /**
     * Tests if a {@link JsonMapper} class instance is able to marshall
     *   an object to a valid JSON format string representation and write
     *   it to a string with identity retention.
     */
    @Test
    @DisplayName("Writes an object to string as a JSON with identity retention")
    void writesObjectToJsonStringWithIdentityRetention() {
        JsonMapper jsonMapper = new JsonMapper(true);

        MapperTestClass obj = new MapperTestClass();
        RefEqualityTestClass ref = new RefEqualityTestClass("ref");

        obj.setRef1(ref);
        obj.setRef2(ref);

        String jsonString = jsonMapper.writeToString(obj);
        String testRegex = "\\{\"ref0\":\\{\"\\$ref\":\"([a-zA-Z0-9-]*)\",\"name\":\"ref\"}," +
                "\"ref2\":\\{\"\\$ref\":\"(\\1)\",\"name\":\"ref\"}}";


        Assertions.assertTrue(Pattern.matches(testRegex, jsonString));
    }

    /**
     * Tests if a {@link JsonMapper} class instance is able to marshall
     *   an object to a valid JSON format string representation and write
     *   it to an {@link java.io.OutputStream} without identity retention.
     */
    @Test
    @DisplayName("Writes an object to stream as a JSON without identity retention")
    void writesObjectToJsonStreamWithoutIdentityRetention() throws IOException {
        JsonMapper jsonMapper = new JsonMapper(false);

        MapperTestClass obj = new MapperTestClass();
        RefEqualityTestClass ref1 = new RefEqualityTestClass("ref1");
        RefEqualityTestClass ref2 = new RefEqualityTestClass("ref2");

        obj.setRef1(ref1);
        obj.setRef2(ref2);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        jsonMapper.write(obj, byteArrayOutputStream);
        String jsonString = byteArrayOutputStream.toString();
        String testString = "{\"ref0\":{\"name\":\"ref1\"},\"ref2\":{\"name\":\"ref2\"}}";

        Assertions.assertEquals(testString, jsonString);
    }

    /**
     * Tests if a {@link JsonMapper} class instance is able to marshall
     *   an object to a valid JSON format string representation and write
     *   it to an {@link java.io.OutputStream} with identity retention.
     */
    @Test
    @DisplayName("Writes an object to stream as a JSON with identity retention")
    void writesObjectToJsonStreamWithIdentityRetention() throws IOException {
        JsonMapper jsonMapper = new JsonMapper(true);

        MapperTestClass obj = new MapperTestClass();
        RefEqualityTestClass ref = new RefEqualityTestClass("ref");

        obj.setRef1(ref);
        obj.setRef2(ref);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        jsonMapper.write(obj, byteArrayOutputStream);
        String jsonString = byteArrayOutputStream.toString();
        String testRegex = "\\{\"ref0\":\\{\"\\$ref\":\"([a-zA-Z0-9-]*)\",\"name\":\"ref\"}," +
                "\"ref2\":\\{\"\\$ref\":\"(\\1)\",\"name\":\"ref\"}}";


        Assertions.assertTrue(Pattern.matches(testRegex, jsonString));
    }

    /**
     * Tests if a {@link JsonMapper} class instance is able to marshall
     *   an object to a valid JSON format string representation and write
     *   it to a {@link File} without identity retention.
     */
    @Test
    @DisplayName("Writes an object to file as a JSON without identity retention")
    void writesObjectToJsonFileWithoutIdentityRetention() throws IOException {
        JsonMapper jsonMapper = new JsonMapper(false);

        URL fileToRead = JsonMapperTest.class.getResource("testOutputJson.json");

        if (fileToRead == null) {
            throw new IllegalStateException("File testOutputJson not found.");
        }

        try {
            File jsonFile = new File(fileToRead.toURI());

            MapperTestClass obj = new MapperTestClass();
            RefEqualityTestClass ref1 = new RefEqualityTestClass("ref1");
            RefEqualityTestClass ref2 = new RefEqualityTestClass("ref2");

            obj.setRef1(ref1);
            obj.setRef2(ref2);

            jsonMapper.write(obj, jsonFile);
            String jsonString = Files.readString(jsonFile.toPath());
            String testString = "{\"ref0\":{\"name\":\"ref1\"},\"ref2\":{\"name\":\"ref2\"}}";

            Assertions.assertEquals(testString, jsonString);
        } catch (URISyntaxException ex) {
            throw new IllegalStateException("File testOutputJson not found.", ex);
        }
    }

    /**
     * Tests if a {@link JsonMapper} class instance is able to marshall
     *   an object to a valid JSON format string representation and write
     *   it to a {@link File} with identity retention.
     */
    @Test
    @DisplayName("Writes an object to file as a JSON with identity retention")
    void writesObjectToJsonFileWithIdentityRetention() throws IOException {
        JsonMapper jsonMapper = new JsonMapper(true);

        URL fileToRead = JsonMapperTest.class.getResource("testOutputJson.json");

        if (fileToRead == null) {
            throw new IllegalStateException("File testOutputJson not found.");
        }

        try {
            File jsonFile = new File(fileToRead.toURI());

            MapperTestClass obj = new MapperTestClass();
            RefEqualityTestClass ref = new RefEqualityTestClass("ref");

            obj.setRef1(ref);
            obj.setRef2(ref);

            jsonMapper.write(obj, jsonFile);
            String jsonString = Files.readString(jsonFile.toPath());
            String testRegex = "\\{\"ref0\":\\{\"\\$ref\":\"([a-zA-Z0-9-]*)\",\"name\":\"ref\"}," +
                    "\"ref2\":\\{\"\\$ref\":\"(\\1)\",\"name\":\"ref\"}}";


            Assertions.assertTrue(Pattern.matches(testRegex, jsonString));
        } catch (URISyntaxException ex) {
            throw new IllegalStateException("File testOutputJson not found.", ex);
        }
    }

    /**
     * Tests if a {@link JsonMapper} class instance validly marshalls and unmarshalls
     *   an object with {@link NullHandling} set to {@code INCLUDE}
     *   and {@link UnknownPropertiesPolicy} set to {@code IGNORE}.
     */
    @Test
    @DisplayName("Correctly writes and reads a JSON of an object with nulls included and unknowns ignored")
    void testNullIncludeUnknownIgnore() {
        JsonMapper jsonMapper = new JsonMapper(false);

        NullIncludeUnknownIgnoreRecord obj = new NullIncludeUnknownIgnoreRecord();

        String testString = "{\"nonNull\":\"not null\",\"nullVal\":null}";
        String jsonText = jsonMapper.writeToString(obj);

        Assertions.assertAll(
                () -> assertEquals(testString, jsonText),
                () -> assertDoesNotThrow(() -> jsonMapper.readFromString(NullIncludeUnknownIgnoreRecord.class,
                        jsonText)),
                () -> assertNull(jsonMapper.readFromString(NullIncludeUnknownIgnoreRecord.class, jsonText).nullVal),
                () -> assertEquals("not null", jsonMapper.readFromString(NullIncludeUnknownIgnoreRecord.class,
                        jsonText).nonNull)
        );
    }

    /**
     * Tests if a {@link JsonMapper} class instance validly marshalls and unmarshalls
     *   an object with {@link NullHandling} set to {@code EXCLUDE}
     *   and {@link UnknownPropertiesPolicy} set to {@code IGNORE}.
     */
    @Test
    @DisplayName("Correctly writes and reads a JSON of an object with nulls excluded and unknowns ignored")
    void testNullExcludeUnknownIgnore() {
        JsonMapper jsonMapper = new JsonMapper(false);

        NullExcludeUnknownIgnoreRecord obj = new NullExcludeUnknownIgnoreRecord();

        String testString = "{\"nonNull\":\"not null\"}";
        String jsonText = jsonMapper.writeToString(obj);

        Assertions.assertAll(
                () -> assertEquals(testString, jsonText),
                () -> assertDoesNotThrow(() -> jsonMapper.readFromString(NullExcludeUnknownIgnoreRecord.class,
                        jsonText)),
                () -> assertNull(jsonMapper.readFromString(NullExcludeUnknownIgnoreRecord.class, jsonText).nullVal),
                () -> assertEquals("not null", jsonMapper.readFromString(NullExcludeUnknownIgnoreRecord.class,
                        jsonText).nonNull)
        );
    }

    /**
     * Tests if a {@link JsonMapper} class instance validly marshalls and unmarshalls
     *   an object with {@link NullHandling} set to {@code INCLUDE}
     *   and {@link UnknownPropertiesPolicy} set to {@code FAIL}.
     */
    @Test
    @DisplayName("Correctly writes and reads a JSON of an object with nulls included and unknowns ignored")
    void testNullIncludeUnknownFail() {
        JsonMapper jsonMapper = new JsonMapper(false);

        NullIncludeUnknownFailRecord obj = new NullIncludeUnknownFailRecord();

        String testString = "{\"nonNull\":\"not null\",\"nullVal\":null}";
        String jsonText = jsonMapper.writeToString(obj);

        Assertions.assertAll(
                () -> assertEquals(testString, jsonText),
                () -> assertThrows(
                        JsonMappingException.class,
                        () -> jsonMapper.readFromString(NullIncludeUnknownFailRecord.class, jsonText)
                )
        );
    }

    /**
     * Tests if a {@link JsonMapper} class instance validly marshalls and unmarshalls
     *   an object with {@link NullHandling} set to {@code EXCLUDE}
     *   and {@link UnknownPropertiesPolicy} set to {@code FAIL}.
     */
    @Test
    @DisplayName("Correctly writes and reads a JSON of an object with nulls included and unknowns ignored")
    void testNullExcludeUnknownFail() {
        JsonMapper jsonMapper = new JsonMapper(false);

        NullExcludeUnknownFailRecord obj = new NullExcludeUnknownFailRecord();

        String testString = "{\"nonNull\":\"not null\"}";
        String jsonText = jsonMapper.writeToString(obj);

        Assertions.assertAll(
                () -> assertEquals(testString, jsonText),
                () -> assertThrows(
                        JsonMappingException.class,
                        () -> jsonMapper.readFromString(NullExcludeUnknownFailRecord.class, jsonText)
                )
        );
    }
}