package dev.vpendischuk.mapper.json;

import dev.vpendischuk.mapper.Mapper;
import dev.vpendischuk.mapper.json.exceptions.JsonMappingException;
import dev.vpendischuk.mapper.json.exceptions.JsonReadException;
import dev.vpendischuk.mapper.json.types.JsonObject;
import dev.vpendischuk.mapper.json.util.DefaultJsonReader;
import dev.vpendischuk.mapper.json.util.JsonReader;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Provides an implementation of the {@code Mapper} interface
 *   that provides functionality for marshalling and unmarshalling
 *   JSONs via mapping them to specified object types.
 * <p>
 * Has a {@code retainIdentity} flag that denotes if reference equality
 *   should be maintained when writing and reading objects to\from a JSON.
 * <p>
 * Initialization example:
 *
 * <pre>
 * // maintains references
 * JsonMapper jsonMapper1 = new JsonMapper(true);
 *
 * // does not maintain references
 * JsonMapper jsonMapper2 = new JsonMapper(false);
 * </pre>
 */
public class JsonMapper implements Mapper {
    // Flags which denotes if references should be maintained.
    private final boolean retainIdentity;

    /**
     * Initializes a new {@code JsonMapper} instance with specified settings.
     * <p>
     * Initialization example:
     *
     * <pre>
     * // maintains references
     * JsonMapper jsonMapper1 = new JsonMapper(true);
     *
     * // does not maintain references
     * JsonMapper jsonMapper2 = new JsonMapper(false);
     * </pre>
     *
     * @param retainIdentity flag that denotes if reference equality
     *                       should be maintained for objects.
     */
    public JsonMapper(boolean retainIdentity) {
        this.retainIdentity = retainIdentity;
    }

    /**
     * Reads {@code clazz} instance from specified {@code input} JSON
     *   string and returns the read instance.
     *
     * <p>
     * Note: Class represented by the {@code clazz} parameter must have
     *   an {@code Exported} annotation and a parameterless constructor,
     *   and also must not be a parameterized type or a non-static inner class.
     * </p>
     * <p>
     * Call example:
     *
     * <pre>
     * &#64;Exported
     * Class Foo {
     *     public String name;
     *
     *     public Foo() {
     *         name = null;
     *     }
     *
     *     public Foo(String name) {
     *         this.name = name;
     *     }
     * }
     *
     * String jsonText = "{\"name\":\"Jason\"}";
     * JsonMapper jsonMapper = new JsonMapper(true);
     * Foo restored = jsonMapper.readFromString(Foo.class, jsonText);
     * </pre>
     *
     * @param clazz class, the instance of which is saved in the JSON.
     * @param input JSON representation of a {@code clazz} instance.
     * @param <T> type of the returned instance.
     * @return {@code clazz} instance saved in the JSON string.
     * @throws JsonReadException if the JSON could not be read or JSON model was invalid.
     */
    @Override
    public <T> T readFromString(Class<T> clazz, String input) throws JsonReadException {
        JsonReader reader = new DefaultJsonReader(input, retainIdentity);
        JsonObject readObject = new JsonObject(reader);
        return readObject.toValue(clazz);
    }

    /**
     * Reads {@code clazz} instance from specified {@code inputStream} JSON
     *   input stream and returns the read instance.
     *
     * <p>
     * Note: Class represented by the {@code clazz} parameter must have
     *   an {@code Exported} annotation and a parameterless constructor,
     *   and also must not be a parameterized type or a non-static inner class.
     * </p>
     * <p>
     * Call example:
     *
     * <pre>
     * &#64;Exported
     * Class Foo {
     *     public String name;
     *
     *     public Foo() {
     *         name = null;
     *     }
     *
     *     public Foo(String name) {
     *         this.name = name;
     *     }
     * }
     *
     * String jsonText = "{\"name\":\"Jason\"}";
     * JsonMapper jsonMapper = new JsonMapper(true);
     * ByteArrayInputStream stream = new ByteArrayInputStream(jsonText.getBytes(StandardCharsets.UTF_8));
     * Foo restored = jsonMapper.read(Foo.class, stream);
     * </pre>
     *
     * @param clazz class, the instance of which is saved in the JSON.
     * @param inputStream input stream that provides a JSON representation of a {@code clazz} instance.
     * @param <T> type of the returned instance.
     * @return {@code clazz} instance saved in the JSON data received from {@code inputStream} input stream.
     * @throws JsonReadException if the JSON could not be read or JSON model was invalid.
     * @throws IOException on input/output error.
     */
    @Override
    public <T> T read(Class<T> clazz, InputStream inputStream) throws JsonReadException, IOException {
        JsonReader reader = new DefaultJsonReader(new InputStreamReader(inputStream), retainIdentity);
        JsonObject readObject = new JsonObject(reader);
        inputStream.close();
        return readObject.toValue(clazz);
    }

    /**
     * Reads {@code clazz} instance from specified {@code file} file that contains
     *   a JSON data string and returns the read instance.
     *
     * <p>
     * Note: Class represented by the {@code clazz} parameter must have
     *   an {@code Exported} annotation and a parameterless constructor,
     *   and also must not be a parameterized type or a non-static inner class.
     * </p>
     * <p>
     * Call example:
     *
     * <pre>
     * &#64;Exported
     * Class Foo {
     *     public String name;
     *
     *     public Foo() {
     *         name = null;
     *     }
     *
     *     public Foo(String name) {
     *         this.name = name;
     *     }
     * }
     *
     * JsonMapper jsonMapper = new JsonMapper(true);
     * Foo restored = jsonMapper.read(Foo.class, new File("/dir/file"));
     * </pre>
     *
     * @param clazz class, the instance of which is saved in the JSON.
     * @param file file that contains a JSON representation of a {@code clazz} instance.
     * @param <T> type of the returned instance.
     * @return {@code clazz} instance saved in the JSON data received from the {@code file} file.
     * @throws JsonReadException if the JSON could not be read or JSON model was invalid.
     */
    @Override
    public <T> T read(Class<T> clazz, File file) throws JsonReadException, IOException {
        FileReader fileReader = new FileReader(file);
        JsonReader reader = new DefaultJsonReader(fileReader, retainIdentity);
        JsonObject readObject = new JsonObject(reader);
        fileReader.close();
        return readObject.toValue(clazz);
    }

    /**
     * Marshals a specified {@code object} object in a JSON and returns it in a string.
     *
     * <p>
     * Note: Class of the {@code object} parameter must have
     *   an {@code Exported} annotation and a parameterless constructor,
     *   and also must not be a parameterized type or a non-static inner class.
     * </p>
     * <p>
     * Call example:
     *
     * <pre>
     * &#64;Exported
     * Class Foo {
     *     public String name;
     *
     *     public Foo() {
     *         name = null;
     *     }
     *
     *     public Foo(String name) {
     *         this.name = name;
     *     }
     * }
     *
     * JsonMapper jsonMapper = new JsonMapper(true);
     * Foo foo = new Foo("Jason");
     * String json = jsonMapper.writeToString(foo);
     * </pre>
     *
     * @param object object that is to be marshalled to a JSON.
     * @return a {@code String} instance that contains the marshalled JSON.
     * @throws JsonMappingException if a JSON model of the object could
     *                              not be created due to an invalid
     *                              object type or a model data state.
     */
    @Override
    public String writeToString(Object object) throws JsonMappingException {
        JsonObject jsonObject = new JsonObject(object, retainIdentity);

        return jsonObject.toString();
    }

    /**
     * Marshals a specified {@code object} object in a JSON and writes a JSON string
     *   to the specified {@code outputStream} output stream.
     *
     * <p>
     * Note: Class of the {@code object} parameter must have
     *   an {@code Exported} annotation and a parameterless constructor,
     *   and also must not be a parameterized type or a non-static inner class.
     * </p>
     * <p>
     * Call example:
     *
     * <pre>
     * &#64;Exported
     * Class Foo {
     *     public String name;
     *
     *     public Foo() {
     *         name = null;
     *     }
     *
     *     public Foo(String name) {
     *         this.name = name;
     *     }
     * }
     *
     * JsonMapper jsonMapper = new JsonMapper(true);
     * Foo foo = new Foo("Jason");
     * jsonMapper.write(foo, new FileOutputStream("/dir/file"));
     * </pre>
     *
     * @param object object that is to be marshalled to a JSON.
     * @param outputStream stream used for JSON data output.
     * @throws JsonMappingException if a JSON model of the object could
     *                              not be created due to an invalid
     *                              object type or a model data state.
     * @throws IOException if an output error has occurred.
     */
    @Override
    public void write(Object object, OutputStream outputStream) throws IOException, JsonMappingException {
        JsonObject jsonObject = new JsonObject(object, retainIdentity);
        byte[] data = jsonObject.toString().getBytes(StandardCharsets.UTF_8);
        outputStream.write(data);
        outputStream.close();
    }

    /**
     * Marshals a specified {@code object} object in a JSON and writes a JSON string
     *   to the specified {@code file} file.
     *
     * <p>
     * Note: Class of the {@code object} parameter must have
     *   an {@code Exported} annotation and a parameterless constructor,
     *   and also must not be a parameterized type or a non-static inner class.
     * </p>
     * <p>
     * Call example:
     *
     * <pre>
     * &#64;Exported
     * Class Foo {
     *     public String name;
     *
     *     public Foo() {
     *         name = null;
     *     }
     *
     *     public Foo(String name) {
     *         this.name = name;
     *     }
     * }
     *
     * JsonMapper jsonMapper = new JsonMapper(true);
     * Foo foo = new Foo("Jason");
     * jsonMapper.write(foo, new File("/dir/file"));
     * </pre>
     *
     * @param object object that is to be marshalled to a JSON.
     * @param file file used for JSON data output.
     * @throws JsonMappingException if a JSON model of the object could
     *                              not be created due to an invalid
     *                              object type or a model data state.
     * @throws IOException if an output error has occurred.
     */
    @Override
    public void write(Object object, File file) throws IOException, JsonMappingException {
        FileOutputStream output = new FileOutputStream(file, false);
        write(object, output);
        output.close();
    }
}
