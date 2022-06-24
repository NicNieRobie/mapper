package dev.vpendischuk.mapper.json.types;

import dev.vpendischuk.mapper.json.util.*;
import dev.vpendischuk.mapper.json.annotations.DateFormat;
import dev.vpendischuk.mapper.json.annotations.Exported;
import dev.vpendischuk.mapper.json.annotations.Ignored;
import dev.vpendischuk.mapper.json.annotations.PropertyName;
import dev.vpendischuk.mapper.json.annotations.enums.NullHandling;
import dev.vpendischuk.mapper.json.annotations.enums.UnknownPropertiesPolicy;
import dev.vpendischuk.mapper.json.exceptions.InvalidTimeFormatException;
import dev.vpendischuk.mapper.json.exceptions.JsonMappingException;
import dev.vpendischuk.mapper.json.exceptions.JsonReadException;
import dev.vpendischuk.mapper.json.types.enums.SupportedType;

import java.lang.reflect.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents an object value in the JSON object model.
 * <p>
 * In order to be mapped to a JsonObject, object must have a type
 *   that is annotated as {@link Exported}, isn't a parameterized type,
 *   is either a record or a class with a parameterless constructor
 *   and is not a non-static inner class.
 */
public class JsonObject extends JsonValue {
    // Map used to store object's field\component names and their JSON model representations.
    private Map<String, JsonValue> modelMap;
    // Reference resolver used by the instance.
    private final JsonMapReferenceResolver mapReferenceResolver;
    // Flag that denotes if reference equality should be maintained.
    private final boolean retainIdentity;

    /**
     * Initializes a new {@link JsonObject} instance as an empty object model.
     *
     * @param referenceResolver reference resolver used to maintain reference equality.
     * @param retainIdentity flag that denotes if reference equality should be maintained.
     */
    public JsonObject(JsonMapReferenceResolver referenceResolver, boolean retainIdentity) {
        modelMap = new TreeMap<>();
        mapReferenceResolver = referenceResolver;
        this.retainIdentity = retainIdentity;
    }

    /**
     * Initializes a new {@link JsonObject} instance as a model of an object
     *   read from input stream via the {@code jsonReader} reader.
     *
     * @param jsonReader a {@link JsonReader} instance.
     * @throws JsonReadException if JSON could not be read successfully.
     */
    public JsonObject(JsonReader jsonReader) throws JsonReadException {
        // Initializing with reader parameters.
        this(jsonReader.getReferenceResolver(), jsonReader.getIdentityRetainFlag());

        char readChar;
        String keyValue = "";

        // Syntax check.
        if (jsonReader.nextCharacterTrimmed() != '{') {
            throw new JsonReadException("[JSON Object Error] Syntax error: no object delimiter bracket found.");
        }

        for (;;) {
            char previousChar = jsonReader.previousCharacter();
            readChar = jsonReader.nextCharacterTrimmed();

            switch (readChar) {
                // EOF check.
                case 0 -> throw new JsonReadException("[JSON Object Error] Syntax error: unexpected end of file.");
                case '}' -> {
                    return;
                }
                case '{', '[' -> {
                    // Syntax check.
                    if (previousChar == '{') {
                        throw new JsonReadException("[JSON Object Error] Syntax error: no object/array key found.");
                    }
                }
                default -> {
                    jsonReader.moveBack();

                    // Reading the key name.
                    JsonValue keyJsonValue = jsonReader.nextValue();

                    // Syntax check.
                    if (!keyJsonValue.isJsonString()) {
                        throw new JsonReadException("[JSON Object Error] Syntax error: invalid key type.");
                    }

                    // Obtaining the key.
                    keyValue = ((JsonString) keyJsonValue).getContent();
                }
            }

            // Syntax check.
            readChar = jsonReader.nextCharacterTrimmed();
            if (readChar != ':') {
                throw new JsonReadException("[JSON Object Error] " +
                        "Syntax error: no expected ':' after the key " + keyValue + ".");
            }

            if (!Objects.isNull(keyValue)) {
                // Duplicate check.
                if (modelMap.containsKey(keyValue)) {
                    throw new JsonReadException("[JSON Object Error] " +
                            "Syntax error: duplicate key " + keyValue + ".");
                }

                // Reading the field value and saving the pair in the model.
                JsonValue value = jsonReader.nextValue();
                if (value != null) {
                    modelMap.put(keyValue, value);
                }
            }

            switch (jsonReader.nextCharacterTrimmed()) {
                case ',' -> {
                    if (jsonReader.nextCharacterTrimmed() == '}') {
                        // End of object.
                        return;
                    }

                    // Continue reading the object.
                    jsonReader.moveBack();
                }
                case '}' -> {
                    // End of object.
                    return;
                }
                // Syntax error detected - invalid character after a value.
                default -> throw new JsonReadException("[JSON Object Error] Syntax error: expected a '}' or a comma.");
            }
        }
    }

    /**
     * Initializes a new {@link JsonObject} instance as a model of the specified object.
     *
     * @param obj object, the model of which this instance will be created as.
     * @param retainIdentity flag that denotes if reference equality should be maintained.
     * @param <T> object type.
     * @throws JsonMappingException if object's JSON model could not be constructed;
     *                              for more info on possible causes see {@link JsonMappingException}.
     * @throws InvalidTimeFormatException if the time format of the object's field was invalid.
     * @see JsonMappingException
     * @see InvalidTimeFormatException
     */
    public <T> JsonObject(T obj, boolean retainIdentity) throws JsonMappingException, InvalidTimeFormatException {
        this(obj, new DefaultJsonMapReferenceResolver(), retainIdentity);
    }

    /**
     * Initializes a new {@link JsonObject} instance as a model of the specified object.
     *
     * @param obj object, the model of which this instance will be created as.
     * @param mapReferenceResolver reference resolver used to maintain reference equality.
     * @param retainIdentity flag that denotes if reference equality should be maintained.
     * @param <T> object type.
     * @throws JsonMappingException if object's JSON model could not be constructed;
     *                              for more info on possible causes see {@link JsonMappingException}.
     * @throws InvalidTimeFormatException if the time format of the object's field was invalid.
     * @see JsonMappingException
     * @see InvalidTimeFormatException
     */
    public <T> JsonObject(T obj, JsonMapReferenceResolver mapReferenceResolver, boolean retainIdentity)
            throws JsonMappingException, InvalidTimeFormatException {
        if (obj == null) {
            throw new JsonMappingException("[JSON Object Error] Object invalid: " +
                    "Exported object was null.");
        }

        checkObjectClassSupport(obj.getClass());

        try {
            if (!obj.getClass().isRecord()) {
                // Suppressing warning as the check is required to avoid
                //   marshalling of classes without parameterless constructors.
                @SuppressWarnings("unused")
                Constructor<?> tConstructor = obj.getClass().getConstructor();
            }
        } catch (NoSuchMethodException ex) {
            throw new JsonMappingException("[JSON Object Error] Object type invalid: " +
                    "Parameterless constructor not found.", ex);
        }

        this.mapReferenceResolver = mapReferenceResolver;
        this.retainIdentity = retainIdentity;

        modelMap = new TreeMap<>();
        modelFromObject(obj);
    }

    /**
     * Checks if the object class is supported for marshalling.
     * <p>
     * Acts as a wrapper for exception raising events.
     *
     * @param objectClass the checked class.
     * @throws JsonMappingException if the object type is unsupported or invalid.
     */
    private void checkObjectClassSupport(Class<?> objectClass) throws JsonMappingException {
        if (!objectClass.isAnnotationPresent(Exported.class)) {
            throw new JsonMappingException("[JSON Object Error] Object type invalid: " +
                    "Exported annotation not found.");
        }

        if (objectClass.getTypeParameters().length != 0) {
            throw new JsonMappingException("[JSON Object Error] Object type invalid: " +
                    "Generic types are not supported.");
        }

        if (objectClass.isMemberClass() && !Modifier.isStatic(objectClass.getModifiers())) {
            throw new JsonMappingException("[JSON Object Error] Object type invalid: " +
                    "Non-static inner classes are not supported.");
        }
    }

    /**
     * Adds the specified key-value pair as a field in the {@code JsonObject} JSON object model.
     *
     * @param key the key value.
     * @param value the value of the field.
     * @throws InvalidTimeFormatException if the time format of the object's field was invalid.
     */
    public void addKeyValuePair(String key, Object value) throws InvalidTimeFormatException {
        Class<?> valueClass = value.getClass();

        processKeyValuePair(key, value, valueClass, true, null);
    }

    /**
     * Creates a JSON model of the specified object {@code obj}.
     *
     * @param obj object to create model of.
     * @param <T> type of the object.
     * @throws InvalidTimeFormatException if the time format of the object's field was invalid.
     */
    private <T> void modelFromObject(T obj) throws InvalidTimeFormatException {
        // Registering the object as a root object while constructing its model.
        mapReferenceResolver.registerModelRootObject(obj);

        Class<?> objClass = obj.getClass();

        // Obtaining the null handling policy.
        NullHandling nullHandlingParam = objClass.getAnnotation(Exported.class).nullHandling();

        if (objClass.isRecord()) {
            // Processing the record components.
            for (final RecordComponent component : objClass.getRecordComponents()) {
                processRecordComponent(component, obj, nullHandlingParam);
            }
        } else {
            // Processing the class fields.
            for (final Field field : objClass.getDeclaredFields()) {
                processField(field, obj, nullHandlingParam);
            }
        }

        // Unregistering the root object when finished.
        mapReferenceResolver.unregisterModelRootObject(obj);
    }

    /**
     * Processes a field of the {@code parentObj} object to include its JSON representation in the model.
     *
     * @param field the field.
     * @param parentObj the field's parent object.
     * @param nullHandlingParam null handling policy.
     * @param <T> parent object's type.
     * @throws InvalidTimeFormatException if the time format of the object's field was invalid.
     */
    private <T> void processField(Field field, T parentObj, NullHandling nullHandlingParam)
            throws InvalidTimeFormatException, JsonMappingException {
        field.setAccessible(true);

        // Skipping synthetic, static and Ignored fields.
        if (field.isSynthetic() ||
                Modifier.isStatic(field.getModifiers()) ||
                field.isAnnotationPresent(Ignored.class)) {
            return;
        }

        // Obtaining the value of the field's name for marshalling.
        String keyName;
        if (field.isAnnotationPresent(PropertyName.class)) {
            String annotationValue = field.getAnnotation(PropertyName.class).value();

            if (annotationValue != null) {
                try {
                    Field foundField = parentObj.getClass().getDeclaredField(annotationValue);

                    if (!foundField.equals(field)) {
                        throw new JsonMappingException("[JSON Object Error] Illegal property naming: " +
                                "Field " + field.getName() + " annotation name duplicates the name of another field.");
                    }
                } catch (NoSuchFieldException ignored) { }
            }

            keyName = Objects.requireNonNullElseGet(annotationValue, field::getName);
        } else {
            keyName = field.getName();
        }

        Class<?> fieldClass = field.getType();

        // Obtaining the field value.
        Object fieldValue;
        try {
            fieldValue = field.get(parentObj);
        } catch (IllegalAccessException ex) {
            return;
        }

        // Obtaining the date format.
        String datetimeFormat = field.isAnnotationPresent(DateFormat.class) ?
                field.getAnnotation(DateFormat.class).value() : null;

        // Processing the key value pair for including it in the model.
        processKeyValuePair(keyName, fieldValue, fieldClass,
                nullHandlingParam == NullHandling.INCLUDE, datetimeFormat);
    }

    /**
     * Processes a component of the {@code parentObj} object to include its JSON representation in the model.
     *
     * @param component the component.
     * @param parentObj the component's parent object.
     * @param nullHandlingParam null handling policy.
     * @param <T> parent object's type.
     * @throws InvalidTimeFormatException if the time format of the object's component was invalid.
     */
    private <T> void processRecordComponent(RecordComponent component, T parentObj,
                                            NullHandling nullHandlingParam) throws InvalidTimeFormatException {
        // Skipping Ignored components.
        if (component.isAnnotationPresent(Ignored.class)) {
            return;
        }

        // Obtaining the value of the component's name for marshalling.
        String keyName;

        List<String> componentNames = Arrays.stream(parentObj.getClass().getRecordComponents())
                .map(RecordComponent::getName).toList();

        if (component.isAnnotationPresent(PropertyName.class)) {
            String annotationValue = component.getAnnotation(PropertyName.class).value();

            if (componentNames.contains(annotationValue)) {
                throw new JsonMappingException("[JSON Object Error] Illegal property naming: " +
                        "Component " + component.getName() + " annotation name " +
                        "duplicates the name of another component.");
            }

            keyName = Objects.requireNonNullElseGet(annotationValue, component::getName);
        } else {
            keyName = component.getName();
        }

        Class<?> componentClass = component.getType();

        // Obtaining the component value.
        Object componentValue;
        try {
            component.getAccessor().setAccessible(true);
            componentValue = component.getAccessor().invoke(parentObj);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            return;
        }

        // Obtaining the date format.
        String datetimeFormat = component.isAnnotationPresent(DateFormat.class) ?
                component.getAnnotation(DateFormat.class).value() : null;

        // Processing the key value pair for including it in the model.
        processKeyValuePair(keyName, componentValue, componentClass,
                nullHandlingParam == NullHandling.INCLUDE, datetimeFormat);
    }

    /**
     * Processes the specified key-value pair to obtain its JSON model and add it to the object's
     *   JSON model map if successful.
     *
     * @param keyName key value.
     * @param value value of the pair.
     * @param valueClass class of the value.
     * @param includeNull null handling policy.
     * @param datetimeFormat datetime format (used if the value is of datetime type).
     * @throws InvalidTimeFormatException if the time format of the value was invalid.
     */
    private void processKeyValuePair(String keyName, Object value, Class<?> valueClass,
                                     boolean includeNull, String datetimeFormat) throws InvalidTimeFormatException {
        // Defining the type of the value's class.
        SupportedType valueTypeClassification = JsonSupportedTypeClassifier.classifyType(valueClass);

        // Ignore nulls if the null handling policy is set to exclude.
        if (!Objects.isNull(value) || includeNull) {
            if (Objects.isNull(value)) {
                modelMap.put(keyName, new JsonNull());
                return;
            }

            switch (valueTypeClassification) {
                case NUMBER -> {
                    // If value is a numerical value.
                    if (Double.class.isAssignableFrom(valueClass)) {
                        // If entry is a floating-point number value.
                        modelMap.put(keyName, new JsonNumber(((Number)value).doubleValue()));
                    } else {
                        // If entry is an integer number value.
                        modelMap.put(keyName, new JsonNumber(((Number)value).longValue()));
                    }
                }
                // If value is a character.
                case CHARACTER -> modelMap.put(keyName, new JsonString((Character) value));
                // If value is a boolean value.
                case BOOLEAN -> modelMap.put(keyName, new JsonBoolean((Boolean) value));
                // For enums and strings - save value as a string.
                case STRING, ENUM -> modelMap.put(keyName, new JsonString(value.toString()));
                case EXPORTED -> {
                    // Obtain object model for Exported types.
                    JsonObject objectModel = new JsonObject(value, mapReferenceResolver, retainIdentity);
                    if (retainIdentity) {
                        mapReferenceResolver.registerObjectModel(value, objectModel);
                    }
                    // Save object model.
                    modelMap.put(keyName, objectModel);
                }
                // If value is a list or a set.
                case LIST, SET -> modelMap.put(keyName, new JsonArray((Collection<?>) value, mapReferenceResolver,
                        includeNull, retainIdentity));
                // If value is a LocalTime value.
                case TIME -> {
                    // Obtaining the time string representation.
                    String timeString = ((LocalTime) value).toString();
                    if (datetimeFormat != null) {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(datetimeFormat);
                        try {
                            timeString = ((LocalTime) value).format(formatter);
                        } catch (UnsupportedTemporalTypeException ex) {
                            throw new InvalidTimeFormatException("[JSON Object Error] Field " + keyName +
                                    " date format invalid.", ex);
                        }
                    }

                    modelMap.put(keyName, new JsonString(timeString));
                }
                // If value is a LocalDate value.
                case DATE -> {
                    // Obtaining the date string representation.
                    String timeString = ((LocalDate) value).toString();
                    if (datetimeFormat != null) {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(datetimeFormat);
                        try {
                            timeString = ((LocalDate) value).format(formatter);
                        } catch (UnsupportedTemporalTypeException ex) {
                            throw new InvalidTimeFormatException("[JSON Object Error] Field " + keyName +
                                    " date format invalid.", ex);
                        }
                    }

                    modelMap.put(keyName, new JsonString(timeString));
                }
                // If value is a LocalDateTime value.
                case DATETIME -> {
                    // Obtaining the datetime string representation.
                    String timeString = ((LocalDateTime) value).toString();
                    if (datetimeFormat != null) {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(datetimeFormat);
                        try {
                            timeString = ((LocalDateTime) value).format(formatter);
                        } catch (UnsupportedTemporalTypeException ex) {
                            throw new InvalidTimeFormatException("[JSON Object Error] Field " + keyName +
                                    " date format invalid.", ex);
                        }
                    }

                    modelMap.put(keyName, new JsonString(timeString));
                }
            }
        }
    }

    /**
     * Returns a JSON representation of the object
     *   represented by the {@code JsonObject} instance.
     *
     * @return JSON representation of an object.
     */
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append('{');

        int index = 0;
        // Appending each value's string representation.
        for (final Map.Entry<String, JsonValue> entry : modelMap.entrySet()) {
            stringBuilder.append('\"');
            if (!Objects.isNull(entry)) {
                stringBuilder.append(entry.getKey());
            } else {
                stringBuilder.append("null");
            }
            stringBuilder.append("\":");
            stringBuilder.append(entry.getValue().toString());

            if (index != modelMap.entrySet().size() - 1) {
                stringBuilder.append(',');
            }

            index++;
        }

        stringBuilder.append('}');

        return stringBuilder.toString();
    }

    /**
     * Restores the model in the {@code JsonObject} instance
     *   in accordance to the {@code objectClass} class:
     * <ul>
     *     <li>restores the names of properties by replacing those from annotations;</li>
     *     <li>remove all redundant fields (except those needed for identity retention).</li>
     * </ul>
     *
     * @param objectClass class of the modelled object.
     */
    private void restoreModel(Class<?> objectClass) {
        // 'Name in model' - 'name in class' map.
        HashMap<String, String> propertyNamesMap = new HashMap<>();

        if (objectClass.isRecord()) {
            // Filling the map with record component names.
            List<RecordComponent> components = Arrays.stream(objectClass.getRecordComponents()).toList();

            for (final RecordComponent component : components) {
                if (component.isAnnotationPresent(PropertyName.class)) {
                    String propertyName = component.getAnnotation(PropertyName.class).value();

                    Optional<RecordComponent> optComponent = components.stream()
                            .filter(x -> x.getName().equals(propertyName)).findFirst();

                    if (optComponent.isPresent()) {
                        RecordComponent foundComponent = optComponent.get();

                        if (!foundComponent.equals(component)) {
                            throw new JsonMappingException("[JSON Object Error] Illegal property naming: " +
                                    "Field " + component.getName() + " annotation name duplicates the name of another field.");
                        }
                    }

                    String keyName = Objects.requireNonNullElse(propertyName, component.getName());
                    propertyNamesMap.put(keyName, component.getName());
                } else {
                    propertyNamesMap.put(component.getName(), component.getName());
                }
            }
        } else {
            // Filling the map with class field names.
            List<Field> fields = Arrays.stream(objectClass.getDeclaredFields()).toList();

            for (final Field field : fields) {
                if (field.isAnnotationPresent(PropertyName.class)) {
                    String propertyName = field.getAnnotation(PropertyName.class).value();

                    try {
                        Field foundField = objectClass.getDeclaredField(propertyName);

                        if (!foundField.equals(field)) {
                            throw new JsonMappingException("[JSON Object Error] Illegal property naming: " +
                                    "Field " + field.getName() + " annotation name duplicates the name of another field.");
                        }
                    } catch (NoSuchFieldException ignored) { }

                    String keyName = Objects.requireNonNullElse(propertyName, field.getName());
                    propertyNamesMap.put(keyName, field.getName());
                } else {
                    propertyNamesMap.put(field.getName(), field.getName());
                }
            }
        }

        // Rebuilding the model with new key names.
        TreeMap<String, JsonValue> newModelMap = new TreeMap<>();
        for (final Map.Entry<String, JsonValue> entry : modelMap.entrySet()) {
            if (entry.getKey().equals("$id") || entry.getKey().equals("$ref")) {
                newModelMap.put(entry.getKey(), entry.getValue());
            } else {
                String propertyName = entry.getKey();
                JsonValue value = entry.getValue();

                if (!propertyNamesMap.containsKey(entry.getKey())) {
                    continue;
                }

                newModelMap.put(propertyNamesMap.get(propertyName), value);
            }
        }

        modelMap = newModelMap;
    }

    /**
     * Returns a valid {@code objectClass} record constructor for the JSON model.
     *
     * @param objectClass object's class.
     * @param tolerateUnknown flag that denotes if the unknown properties policy is set to ignore.
     * @return a valid constructor.
     * @throws JsonMappingException if a valid constructor could not be found.
     */
    private Constructor<?> getRecordConstructor(Class<?> objectClass, boolean tolerateUnknown)
            throws JsonMappingException {
        Constructor<?> constructor = null;

        try {
            if (!objectClass.isRecord()) {
                constructor = objectClass.getConstructor();
                constructor.setAccessible(true);
            } else {
                Set<String> keys = modelMap.keySet();

                // Finding the constructor that is valid for available values.
                for (final Constructor<?> recordConstructor : objectClass.getDeclaredConstructors()) {
                    if (recordConstructor.getParameters().length == 0 && modelMap.size() != 0) {
                        continue;
                    }

                    boolean isValidConstructor = true;
                    for (final Parameter param : recordConstructor.getParameters()) {
                        String paramName = param.getName();

                        if (!keys.contains(paramName) && !tolerateUnknown) {
                            isValidConstructor = false;
                            break;
                        }
                    }

                    if (isValidConstructor) {
                        constructor = recordConstructor;
                        constructor.setAccessible(true);
                    }
                }
            }
        } catch (NoSuchMethodException ex) {
            throw new JsonMappingException("[JSON Object Error] Object model invalid: " +
                    "Record class " + objectClass.getName() + " constructor not found for presented arguments.");
        }

        if (constructor == null) {
            throw new JsonMappingException("[JSON Object Error] Object model invalid: " +
                    "Record class " + objectClass.getName() + " constructor not found for presented arguments.");
        }

        return constructor;
    }

    /**
     * Converts the {@link JsonObject} instance
     *   into the {@code objectClass} instance if the conversion is valid.
     *
     * @param objectClass class that the value will be cast to.
     * @param <T> type of the converted value (inferred from the {@code objectClass} class).
     * @return the converted value.
     * @throws JsonMappingException if the conversion wasn't valid (see the exception description).
     * @see JsonMappingException
     */
    @Override
    public <T> T toValue(Class<T> objectClass) throws JsonMappingException {
        checkObjectClassSupport(objectClass);

        boolean tolerateUnknown =
                objectClass.getAnnotation(Exported.class).unknownPropertiesPolicy() == UnknownPropertiesPolicy.IGNORE;

        // Restoring the original state for the model.
        restoreModel(objectClass);

        Constructor<?> constructor;

        // Obtaining a valid constructor.
        if (objectClass.isRecord()) {
            constructor = getRecordConstructor(objectClass, tolerateUnknown);
        } else {
            try {
                constructor = objectClass.getConstructor();
                constructor.trySetAccessible();
            } catch (NoSuchMethodException ex) {
                throw new JsonMappingException("[JSON Object Error] Object type invalid: " +
                        "Class " + objectClass.getName() + " parameterless constructor not found.");
            }
        }

        T object = null;

        // Initializing the object if its type is not a record.
        try {
            if (!objectClass.isRecord()) {
                object = objectClass.cast(constructor.newInstance());
            }
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException ex) {
            throw new JsonMappingException("[JSON Object Error] Object mapping error: " +
                    "Could not instantiate object of class " + objectClass + ".", ex);
        }

        List<String> classFieldNames;

        // Names of fields that are required to be processed.
        classFieldNames = Arrays.stream(objectClass.getDeclaredFields()).map(Field::getName).toList();
        // Values for record initialization.
        HashMap<String, Object> recordValues = new HashMap<>();

        // Parsing the model.
        for (final String requiredFieldName : classFieldNames) {
            JsonValue value = modelMap.get(requiredFieldName);

            try {
                Field field = objectClass.getDeclaredField(requiredFieldName);

                if (field.isSynthetic() || Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
            } catch (NoSuchFieldException ignored) { }

            if (value == null) {
                if (tolerateUnknown) {
                    if (!objectClass.isRecord()) {
                        try {
                            Field field = objectClass.getDeclaredField(requiredFieldName);
                            field.set(object, null);
                        } catch (NoSuchFieldException | IllegalAccessException ignored) { }
                    } else {
                        recordValues.put(requiredFieldName, null);
                    }
                } else {
                    throw new JsonMappingException("[JSON Object Error] Object mapping error: " +
                            "Value for field " + requiredFieldName + " was null.");
                }
            } else {
                if (!objectClass.isRecord()) {
                    try {
                        Field field = objectClass.getDeclaredField(requiredFieldName);
                        field.trySetAccessible();
                        field.set(object, parseModelValueIntoField(value, field));
                    } catch (NoSuchFieldException | IllegalAccessException ignored) { }
                } else {
                    Optional<Parameter> paramOpt = Arrays.stream(constructor.getParameters())
                            .filter(x -> x.getName().equals(requiredFieldName))
                            .findFirst();

                    if (paramOpt.isEmpty()) {
                        continue;
                    }

                    Parameter param = paramOpt.get();
                    recordValues.put(requiredFieldName, parseModelValueIntoParam(value, param));
                }
            }
        }

        // If model references a value - initialize it with a reference.
        if (modelMap.containsKey("$ref") && !objectClass.isRecord()) {
            UUID id = UUID.nameUUIDFromBytes(((JsonString) modelMap.get("$ref")).getContent().getBytes());
            T referencedObject = objectClass.cast(mapReferenceResolver.getObjectReference(id));

            if (referencedObject == null) {
                mapReferenceResolver.registerObjectReference(id, new AtomicReference<>(object));
            } else {
                return referencedObject;
            }
        }

        // Initializing the object if it is a record.
        if (objectClass.isRecord()) {
            List<Object> paramList = new ArrayList<>();

            for (final Parameter param : constructor.getParameters()) {
                Object paramObject = recordValues.get(param.getName());

                if (paramObject == null && !tolerateUnknown) {
                    throw new JsonMappingException("[JSON Object Error] Object mapping error: " +
                            "Value of record component " + param.getName() + " was null.");
                }

                paramList.add(paramObject);
            }

            try {
                Object instance = constructor.newInstance(paramList.toArray());
                object = objectClass.cast(instance);

                if (modelMap.containsKey("$ref")) {
                    UUID id = UUID.nameUUIDFromBytes(((JsonString) modelMap.get("$ref")).getContent().getBytes());
                    T referencedObject = objectClass.cast(mapReferenceResolver.getObjectReference(id));

                    if (referencedObject == null) {
                        mapReferenceResolver.registerObjectReference(id, new AtomicReference<>(object));
                    } else {
                        object = referencedObject;
                    }
                }
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException ex) {
                throw new JsonMappingException("[JSON Object Error] Object mapping error: " +
                        "Could not instantiate object of class " + objectClass + ".", ex);
            }
        }

        return object;
    }

    /**
     * Parses the value represented by a {@link JsonValue} object to create
     *   a value for the specified {@link Field} field.
     *
     * @param value value JSON representation.
     * @param field class field to be initialized.
     * @return parsed value.
     * @throws JsonMappingException if value could not be parsed;
     *                              for reasons see {@link JsonMappingException}.
     * @see JsonMappingException
     */
    private Object parseModelValueIntoField(JsonValue value, Field field) throws JsonMappingException {
        Class<?> valueClass = field.getType();

        if (Collection.class.isAssignableFrom(valueClass)) {
            ParameterizedType collectionType = (ParameterizedType)field.getGenericType();

            if (!(value instanceof JsonArray)) {
                throw new JsonMappingException("[JSON Object Error] Object mapping error: " +
                        "Field type mismatch for " + field.getName() + " - expected array.");
            }

            return ((JsonArray) value).getCollectionFromValue(valueClass, collectionType);
        } else if (valueClass.isEnum()) {
            if (!(value instanceof JsonString)) {
                throw new JsonMappingException("[JSON Object Error] Object mapping error: " +
                        "Field type mismatch for " + field.getName() + " - expected enum.");
            }
            String valueContent = ((JsonString) value).getContent();
            return Enum.valueOf(valueClass.asSubclass(Enum.class), valueContent);
        } else if (TypeResolver.isLocalTime(valueClass) ||
                TypeResolver.isLocalDate(valueClass) ||
                TypeResolver.isLocalDateTime(valueClass)) {
            String dateFormat = null;
            if (field.isAnnotationPresent(DateFormat.class)) {
                dateFormat = field.getAnnotation(DateFormat.class).value();
            }

            return parseDateTimeJsonValue(value, valueClass, dateFormat);
        } else {
            return value.toValue(valueClass);
        }
    }

    /**
     * Parses the value represented by a {@link JsonValue} object to create
     *   a value for the specified {@link Parameter} parameter.
     *
     * @param value value JSON representation.
     * @param param parameter to be initialized.
     * @return parsed value.
     * @throws JsonMappingException if value could not be parsed;
     *                              for reasons see {@link JsonMappingException}.
     * @see JsonMappingException
     */
    private Object parseModelValueIntoParam(JsonValue value, Parameter param) throws JsonMappingException {
        Class<?> valueClass = param.getType();

        if (Collection.class.isAssignableFrom(valueClass)) {
            ParameterizedType collectionType = (ParameterizedType)param.getParameterizedType();

            if (!(value instanceof JsonArray)) {
                throw new JsonMappingException("[JSON Object Error] Object mapping error: " +
                        "Field type mismatch for " + param.getName() + " - expected array.");
            }

            return ((JsonArray) value).getCollectionFromValue(valueClass, collectionType);
        } else if (valueClass.isEnum()) {
            if (!(value instanceof JsonString)) {
                throw new JsonMappingException("[JSON Object Error] Object mapping error: " +
                        "Field type mismatch for " + param.getName() + " - expected enum.");
            }
            String valueContent = ((JsonString) value).getContent();
            return Enum.valueOf(valueClass.asSubclass(Enum.class), valueContent);
        } else if (TypeResolver.isLocalTime(valueClass) ||
                TypeResolver.isLocalDate(valueClass) ||
                TypeResolver.isLocalDateTime(valueClass)) {
            String dateFormat = null;
            if (param.isAnnotationPresent(DateFormat.class)) {
                dateFormat = param.getAnnotation(DateFormat.class).value();
            }

            return parseDateTimeJsonValue(value, valueClass, dateFormat);
        } else {
            return value.toValue(valueClass);
        }
    }
}
