package dev.vpendischuk.mapper.json.types;

import dev.vpendischuk.mapper.json.types.enums.SupportedType;

/**
 * Class used for defining the {@link SupportedType} category
 *   of a class for mapping it to valid {@link JsonValue} subclass.
 */
public final class JsonSupportedTypeClassifier {
    /**
     * Classifies the provided {@code type} class in accordance
     *   to the {@link SupportedType} enumeration.
     *
     * @param type the class to be classified.
     * @return a {@link SupportedType} enum value that denotes the category of the type for the mapper.
     */
    public static SupportedType classifyType(Class<?> type) {
        if (TypeResolver.isReflectedAsNumber(type)) {
            return SupportedType.NUMBER;
        } else if (TypeResolver.isCharacter(type)) {
            return SupportedType.CHARACTER;
        } else if (TypeResolver.isBoolean(type)) {
            return SupportedType.BOOLEAN;
        } else if (TypeResolver.isString(type)) {
            return SupportedType.STRING;
        } else if (TypeResolver.isEnum(type)) {
            return SupportedType.ENUM;
        } else if (TypeResolver.isList(type)) {
            return SupportedType.LIST;
        } else if (TypeResolver.isSet(type)) {
            return SupportedType.SET;
        } else if (TypeResolver.isLocalTime(type)) {
            return SupportedType.TIME;
        } else if (TypeResolver.isLocalDate(type)) {
            return SupportedType.DATE;
        } else if (TypeResolver.isLocalDateTime(type)) {
            return SupportedType.DATETIME;
        } else if (TypeResolver.isExported(type)) {
            return SupportedType.EXPORTED;
        } else {
            return SupportedType.NOT_SUPPORTED;
        }
    }
}
