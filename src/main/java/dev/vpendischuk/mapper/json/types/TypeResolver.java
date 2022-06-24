package dev.vpendischuk.mapper.json.types;

import dev.vpendischuk.mapper.json.annotations.Exported;

import java.lang.reflect.ParameterizedType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * Class used for resolving type categories and
 *   {@link ParameterizedType} collection parameters.
 */
public class TypeResolver {
    // Set of number primitive types.
    private final static Set<Class<?>> NUMBER_PRIMITIVES;
    static {
        // Filling the number primitives set.
        Set<Class<?>> primitivesSet = new HashSet<>();
        primitivesSet.add(byte.class);
        primitivesSet.add(short.class);
        primitivesSet.add(int.class);
        primitivesSet.add(long.class);
        primitivesSet.add(float.class);
        primitivesSet.add(double.class);
        NUMBER_PRIMITIVES = primitivesSet;
    }

    /**
     * Resolves the class of elements of the collection represented by
     *   the {@link ParameterizedType} type.
     * <p>
     * Call example:
     * <pre>
     * {@code
     * List<String> list = new ArrayList<>();
     * ParameterizedType listType = (ParameterizedType)list.getClass().getGenericSuperclass();
     *
     * // parameterClass will be java.lang.String
     * Class<?> parameterClass = TypeResolver.resolveCollectionType(listType);
     * }
     * </pre>
     *
     * @param type the {@link ParameterizedType} that represents the collection type.
     * @return class of elements of the collection.
     */
    public static Class<?> resolveCollectionType(ParameterizedType type) {
        return (Class<?>)type.getActualTypeArguments()[0];
    }

    /**
     * Checks if the specified {@code type} class is a number wrapper or a number primitive.
     *
     * @param type the class to be checked.
     * @return a {@code boolean} value - {@code true} if the class is a number wrapper or primitive.
     */
    public static boolean isReflectedAsNumber(Class<?> type) {
        return Number.class.isAssignableFrom(type) || NUMBER_PRIMITIVES.contains(type);
    }

    /**
     * Checks if the specified {@code type} class is a character wrapper or a character primitive.
     *
     * @param type the class to be checked.
     * @return a {@code boolean} value - {@code true} if the class is a character wrapper or primitive.
     */
    public static boolean isCharacter(Class<?> type) {
        return type.equals(Character.class) || type.equals(char.class);
    }

    /**
     * Checks if the specified {@code type} class is annotated as {@link Exported}.
     *
     * @param type the class to be checked.
     * @return a {@code boolean} value - {@code true} if the class is annotated as {@link Exported}.
     */
    public static boolean isExported(Class<?> type) {
        return type.isAnnotationPresent(Exported.class);
    }

    /**
     * Checks if the specified {@code type} class implements a {@link List} interface.
     *
     * @param type the class to be checked.
     * @return a {@code boolean} value - {@code true} if the class implements a {@link List} interface.
     */
    public static boolean isList(Class<?> type) {
        return List.class.isAssignableFrom(type);
    }

    /**
     * Checks if the specified {@code type} class implements a {@link Set} interface.
     *
     * @param type the class to be checked.
     * @return a {@code boolean} value - {@code true} if the class implements a {@link Set} interface.
     */
    public static boolean isSet(Class<?> type) {
        return Set.class.isAssignableFrom(type);
    }

    /**
     * Checks if the specified {@code type} class is a {@link String}.
     *
     * @param type the class to be checked.
     * @return a {@code boolean} value - {@code true} if the class is a {@link String}.
     */
    public static boolean isString(Class<?> type) {
        return type.equals(String.class);
    }

    /**
     * Checks if the specified {@code type} class is an enum.
     *
     * @param type the class to be checked.
     * @return a {@code boolean} value - {@code true} if the class is an enum.
     */
    public static boolean isEnum(Class<?> type) {
        return type.isEnum();
    }

    /**
     * Checks if the specified {@code type} class is a boolean wrapper or a boolean primitive.
     *
     * @param type the class to be checked.
     * @return a {@code boolean} value - {@code true} if the class is a boolean wrapper or primitive.
     */
    public static boolean isBoolean(Class<?> type) {
        return type.equals(Boolean.class) || type.equals(boolean.class);
    }

    /**
     * Checks if the specified {@code type} class is a {@link LocalDate}.
     *
     * @param type the class to be checked.
     * @return a {@code boolean} value - {@code true} if the class is a {@link LocalDate}.
     */
    public static boolean isLocalDate(Class<?> type) {
        return LocalDate.class.isAssignableFrom(type);
    }

    /**
     * Checks if the specified {@code type} class is a {@link LocalTime}.
     *
     * @param type the class to be checked.
     * @return a {@code boolean} value - {@code true} if the class is a {@link LocalTime}.
     */
    public static boolean isLocalTime(Class<?> type) {
        return LocalTime.class.isAssignableFrom(type);
    }

    /**
     * Checks if the specified {@code type} class is a {@link LocalDateTime}.
     *
     * @param type the class to be checked.
     * @return a {@code boolean} value - {@code true} if the class is a {@link LocalDateTime}.
     */
    public static boolean isLocalDateTime(Class<?> type) {
        return LocalDateTime.class.isAssignableFrom(type);
    }
}
