package dev.vpendischuk.mapper.json.types.enums;

/**
 * Enumeration which contains flags for every type supported
 *   or unsupported by the {@code JsonMapper} for marshalling
 *   and unmarshalling to a JSON:
 * <ul>
 *     <li>{@code NUMBER} - number primitives and wrappers
 *       ({@code int} - {@code Integer}, {@code short} - {@code Short} etc.);</li>
 *     <li>{@code BOOLEAN} - {@code boolean} primitive and {@code Boolean} wrapper;</li>
 *     <li>{@code CHARACTER} - {@code char} primitive and {@code Character} wrapper;</li>
 *     <li>{@code STRING} - {@code String} type;</li>
 *     <li>{@code ENUM} - {@code enum} types;</li>
 *     <li>{@code EXPORTED} - types annotated as {@code Exported};</li>
 *     <li>{@code LIST} - types that implement the {@code List<T>} interface;</li>
 *     <li>{@code SET} - types that implement the {@code Set<T>} interface;</li>
 *     <li>{@code TIME} - {@code LocalTime} type;</li>
 *     <li>{@code DATE} - {@code LocalDate} type;</li>
 *     <li>{@code DATETIME} - {@code LocalDateTime} type;</li>
 *     <li>{@code NOT_SUPPORTED} - type that is not supported by the {@code JsonMapper}.</li>
 * </ul>
 */
public enum SupportedType {
    NUMBER,
    BOOLEAN,
    CHARACTER,
    STRING,
    ENUM,
    EXPORTED,
    LIST,
    SET,
    TIME,
    DATE,
    DATETIME,
    NOT_SUPPORTED
}
