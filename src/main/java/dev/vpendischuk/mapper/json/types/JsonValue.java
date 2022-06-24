package dev.vpendischuk.mapper.json.types;

import dev.vpendischuk.mapper.json.exceptions.InvalidTimeFormatException;
import dev.vpendischuk.mapper.json.exceptions.JsonMappingException;
import dev.vpendischuk.mapper.json.util.DateFormatValidator;
import dev.vpendischuk.mapper.json.util.DefaultDateFormatValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Abstract class that represents a generic JSON object model value.
 * <p>
 * Provides a common interface for converting the JSON value into an
 *   instance of a class via the {@code toValue} method.
 * <p>
 * Conversion call example:
 * <pre>
 * JsonValue jsonValue = new JsonString("text");
 * String valueText = jsonValue.toValue(String.class);
 *
 * // this will cause a JsonMappingException due to type mismatch
 * Integer valueText = jsonValue.toValue(Integer.class);
 * </pre>
 */
public abstract class JsonValue {
    /**
     * Checks if the {@link JsonValue} instance is a {@link JsonString} instance.
     *
     * @return a {@code boolean} value - {@code true} if instance is an instance of {@link JsonString}.
     */
    public boolean isJsonString() {
        return this instanceof JsonString;
    }

    /**
     * An interface for converting the {@link JsonValue} instance
     *   into the {@code objectClass} instance if the conversion is valid.
     *
     * @param objectClass class that the value will be cast to.
     * @param <T> type of the converted value (inferred from the {@code objectClass} class).
     * @return the converted value.
     * @throws JsonMappingException if the conversion wasn't valid (see the exception description).
     * @see JsonMappingException
     */
    public abstract <T> Object toValue(Class<T> objectClass) throws JsonMappingException;

    /**
     * Converts the value represented by a string into a {@link JsonValue} instance
     *   if it doesn't represent an object or a collection.
     *
     * @param string {@link String} object that contains the value.
     * @return a {@link JsonValue} instance.
     * @throws JsonMappingException if the string was empty.
     */
    public static JsonValue stringToPrimitiveJsonValue(String string) throws JsonMappingException {
        if (string.isEmpty()) {
            throw new JsonMappingException("[JSON Value Error] " +
                    "Could not convert empty string to JSON value.");
        }

        if (string.equalsIgnoreCase("true")) {
            return new JsonBoolean(true);
        } else if (string.equalsIgnoreCase("false")) {
            return new JsonBoolean(false);
        } else if (string.equalsIgnoreCase("null")) {
            return new JsonNull();
        }

        char firstChar = string.charAt(0);

        if ((firstChar >= '0' && firstChar <= '9') || firstChar == '-') {
            return new JsonNumber(string);
        }

        return new JsonString(string);
    }

    /**
     * Parses the datetime value in the {@link JsonValue} in accordance to the specified
     *   {@code valueClass} temporal class of the value and the date format.
     *
     * @param value value to be parsed.
     * @param valueClass class of the datetime value.
     * @param dateFormat date format used for parsing data.
     * @return parsed datetime object.
     * @throws JsonMappingException on type mismatch.
     * @throws InvalidTimeFormatException on invalid date format.
     */
    public static Object parseDateTimeJsonValue(JsonValue value, Class<?> valueClass, String dateFormat)
            throws JsonMappingException, InvalidTimeFormatException {
        boolean isTime = TypeResolver.isLocalTime(valueClass);
        boolean isDate = TypeResolver.isLocalDate(valueClass);
        boolean isDateTime = TypeResolver.isLocalDateTime(valueClass);

        String expectedType = isTime ? "time" : isDate ? "date" : "datetime";

        if ((!(value instanceof JsonString) && !(value instanceof JsonNumber)) ||
                (!isTime && !isDate && !isDateTime)) {
            throw new JsonMappingException("[JSON Object Error] Object mapping error: " +
                    "Type mismatch - expected " + expectedType + ".");
        }

        String valueContent;

        if (value instanceof JsonString) {
            valueContent = ((JsonString) value).getContent();
        } else {
            valueContent = value.toString();
        }

        if (!Objects.isNull(dateFormat)) {
            DateFormatValidator validator = new DefaultDateFormatValidator(dateFormat);

            boolean isValidFormat;

            if (isTime) {
                isValidFormat = validator.timeIsValid(valueContent);
            } else if (isDate) {
                isValidFormat = validator.dateIsValid(valueContent);
            } else {
                isValidFormat = validator.dateTimeIsValid(valueContent);
            }

            if (!isValidFormat) {
                throw new InvalidTimeFormatException("[JSON Object Error] Object mapping error: " +
                        "Date format was invalid.");
            }

            if (isTime) {
                return LocalTime.parse(valueContent, DateTimeFormatter.ofPattern(dateFormat));
            } else if (isDate) {
                return LocalDate.parse(valueContent, DateTimeFormatter.ofPattern(dateFormat));
            } else {
                return LocalDateTime.parse(valueContent, DateTimeFormatter.ofPattern(dateFormat));
            }
        } else {
            if (isTime) {
                return LocalTime.parse(valueContent);
            } else if (isDate) {
                return LocalDate.parse(valueContent);
            } else {
                return LocalDateTime.parse(valueContent);
            }
        }
    }
}
