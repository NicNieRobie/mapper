package dev.vpendischuk.mapper.json.types;

import dev.vpendischuk.mapper.json.util.JsonReader;
import dev.vpendischuk.mapper.json.exceptions.JsonMappingException;
import dev.vpendischuk.mapper.json.exceptions.JsonReadException;
import dev.vpendischuk.mapper.json.types.enums.SupportedType;
import dev.vpendischuk.mapper.json.util.JsonMapReferenceResolver;

import java.lang.reflect.ParameterizedType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * Represents a generic array in a JSON object model.
 * <p>
 * Contains a collection of {@code JsonValue} objects marshalled
 *   from an object or unmarshalled from a JSON.
 * <p>
 * Requires a {@code JsonMapReferenceResolver} for resolving references
 *   in a collection if the {@code retainIdentity} flag is set to {@code true}.
 * <p>
 * <b>Note</b>: Can only contain JSON models of types that are listed as supported in
 *   the {@code SupportedType} enum.
 */
public final class JsonArray extends JsonValue {
    // Collection of JsonValue instances contained in the array.
    private final Collection<JsonValue> collection;
    // JsonMapReferenceResolver reference.
    private final JsonMapReferenceResolver mapReferenceResolver;
    // Flag that denotes if null values should be retained when reading a JSON array.
    private final boolean retainNull;
    // Flag that denotes if object reference equality should be maintained.
    private final boolean retainIdentity;

    /**
     * Initializes a new {@code JsonArray} instance with specified settings.
     *
     * @param referenceResolver a {@code JsonMapReferenceResolver} instance used for
     *                          resolving object references when constructing an array.
     * @param retainIdentity a {@code boolean} value that denotes if reference equality
     *                       should be maintained when constructing an array.
     */
    public JsonArray(JsonMapReferenceResolver referenceResolver, boolean retainIdentity) {
        // Initializing the array and settings.
        this.collection = new ArrayList<>();
        this.retainNull = true;
        this.retainIdentity = retainIdentity;
        this.mapReferenceResolver = referenceResolver;
    }

    /**
     * Initializes a new {@code JsonArray} instance with data read from specified {@code JsonReader}.
     *
     * @param jsonReader the {@code JsonReader} instance used to read the marshalled JSON data
     *                   and construct the array from read data.
     * @throws JsonReadException if a syntax error was encountered when reading data.
     */
    public JsonArray(JsonReader jsonReader) throws JsonReadException {
        // Obtaining mapper identity settings and a reference resolver from the reader.
        this(jsonReader.getReferenceResolver(), jsonReader.getIdentityRetainFlag());

        if (jsonReader.nextCharacterTrimmed() != '[') {
            throw new JsonReadException("[JSON Array Error] Syntax error: no array delimiter bracket found.");
        }

        char readChar = jsonReader.nextCharacterTrimmed();

        if (readChar == 0) {
            throw new JsonReadException("[JSON Array Error] Syntax error: expected ']' or a comma.");
        }

        // While the array is not closed.
        if (readChar != ']') {
            jsonReader.moveBack();
            for (;;) {
                if (jsonReader.nextCharacterTrimmed() == ',') {
                    // If a comma was encountered without a value.
                    throw new JsonReadException("[JSON Array Error] Syntax error: no value provided.");
                } else {
                    // Reading the next JsonValue from JSON.
                    jsonReader.moveBack();
                    JsonValue value = jsonReader.nextValue();
                    collection.add(value);
                }

                switch (jsonReader.nextCharacterTrimmed()) {
                    case ',' -> {
                        // If the next character is a comma.
                        readChar = jsonReader.nextCharacterTrimmed();

                        // Unexpected EOF.
                        if (readChar == 0) {
                            throw new JsonReadException("[JSON Array Error] Syntax error: expected ']' or a comma.");
                        }

                        // If array is closed - stop reading.
                        if (readChar == ']') {
                            return;
                        }
                        jsonReader.moveBack();
                    }
                    case ']' -> {
                        // If array is closed - stop reading.
                        return;
                    }
                    // Unexpected char between the objects.
                    default -> throw new JsonReadException("[JSON Array Error] Syntax error: expected ']' or a comma.");
                }
            }
        }
    }

    /**
     * Initializes a new {@code JsonArray} instance from specified collection.
     *
     * @param collection {@code Collection<T>} collection that contains values
     *                   to be put into the array.
     * @param mapReferenceResolver a {@code JsonMapReferenceResolver} instance used for
     *                             resolving object references when constructing an array.
     * @param retainIdentity a {@code boolean} value that denotes if reference equality
     *                       should be maintained when constructing an array.
     * @param retainNull a {@code boolean} value that denotes if {@code null} values
     *                       should be retained when constructing an array.
     */
    public JsonArray(Collection<?> collection, JsonMapReferenceResolver mapReferenceResolver,
                     boolean retainNull, boolean retainIdentity) {
        this.collection = new ArrayList<>();
        this.retainNull = retainNull;
        this.retainIdentity = retainIdentity;
        this.mapReferenceResolver = mapReferenceResolver;
        constructArray(collection);
    }

    /**
     * Constructs an array with values from specified {@code collection} collection.
     *
     * @param collection collection which contains values to be mapped to {@code JsonValue} instances
     *                   and saved in an array.
     */
    private void constructArray(Collection<?> collection) {
        if (collection == null) {
            return;
        }

        for (final Object entry: collection) {
            if (entry == null) {
                if (retainNull) {
                    // Add a JsonNull value is nulls are retained.
                    this.collection.add(new JsonNull());
                }
                continue;
            }

            // Defining the entry type class for mapping.
            Class<?> entryClass = entry.getClass();
            SupportedType typeClassifier = JsonSupportedTypeClassifier.classifyType(entryClass);

            switch (typeClassifier) {
                case NUMBER -> {
                    if (Double.class.isAssignableFrom(entryClass)) {
                        // If entry is a floating-point number value.
                        this.collection.add(new JsonNumber((Double) entry));
                    } else {
                        // If entry is an integer value.
                        this.collection.add(new JsonNumber((Long) entry));
                    }
                }
                // If entry is a character.
                case CHARACTER -> this.collection.add(new JsonString((Character) entry));
                // If entry is a boolean value.
                case BOOLEAN -> this.collection.add(new JsonBoolean((Boolean) entry));
                // For enums and strings - save value as a string.
                case STRING, ENUM -> this.collection.add(new JsonString(entry.toString()));
                case EXPORTED -> {
                    // Create a JSON object model for an Exported instance.
                    JsonObject objectModel = new JsonObject(entry, mapReferenceResolver, retainIdentity);

                    // Register object model representation.
                    if (retainIdentity) {
                        mapReferenceResolver.registerObjectModel(entry, objectModel);
                    }

                    // Save object model.
                    this.collection.add(objectModel);
                }
                // For collection types - save an array model.
                case LIST, SET -> this.collection.add(new JsonArray((Collection<?>)entry,
                        mapReferenceResolver, retainNull, retainIdentity));
                // For date types - save as a string.
                case TIME -> this.collection.add(new JsonString(((LocalTime) entry).toString()));
                case DATE -> this.collection.add(new JsonString(((LocalDate) entry).toString()));
                case DATETIME -> this.collection.add(new JsonString(((LocalDateTime) entry).toString()));
            }
        }
    }

    /**
     * Returns a string representation of a JSON array in JSON format.
     * <p>
     * Example:
     * <pre>
     * "[null, 2.3, 5.21]" // with null retention
     * "[{...}, {...}]" // object array
     * </pre>
     *
     * @return string representation of a JSON array.
     */
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append('[');

        // Saving collection contents representation to a string.
        Object[] contentArray = collection.toArray();
        for (int i = 0; i < contentArray.length; ++i) {
            // Appending JsonValue string representation.
            if (!Objects.isNull(contentArray[i])) {
                stringBuilder.append(contentArray[i].toString());
            } else {
                stringBuilder.append("null");
            }

            // Comma separator.
            if (i != contentArray.length - 1) {
                stringBuilder.append(',');
            }
        }

        stringBuilder.append(']');

        return stringBuilder.toString();
    }

    /**
     * Tries to convert a JSON array into a {@code Collection} of elements of class {@code objectClass}.
     *
     * @param objectClass class of elements of the collection.
     * @param <T> generic type of elements of the collection.
     * @return collection of array elements that were successfully cast to specified class.
     * @throws JsonMappingException if any object of an array could not be cast to specified class.
     */
    @Override
    public <T> Collection<T> toValue(Class<T> objectClass) throws JsonMappingException {
        Collection<T> values = new ArrayList<>();

        for (final JsonValue entry : collection) {
            // Trying to cast an entry to specified class.
            Object value = entry.toValue(objectClass);

            if (value != null && !(objectClass.isAssignableFrom(value.getClass()))) {
                continue;
            }

            values.add(objectClass.cast(value));
        }

        return values;
    }

    /**
     * Parses a {@code JsonArray} value into a collection given its class and a parameterized type.
     *
     * @param valueClass class of the collection.
     * @param collectionType parameterized type of the collection.
     * @return converted collection.
     */
    public Object getCollectionFromValue(Class<?> valueClass, ParameterizedType collectionType) {
        Class<?> contentClass = TypeResolver.resolveCollectionType(collectionType);
        Collection<?> collection = toValue(contentClass);
        if (Set.class.isAssignableFrom(valueClass)) {
            return new HashSet<>(collection);
        } else {
            return new ArrayList<>(collection);
        }
    }
}
