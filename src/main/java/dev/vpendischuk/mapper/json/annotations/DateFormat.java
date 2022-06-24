package dev.vpendischuk.mapper.json.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used for denoting the format in which the {@code LocalTime},
 *   {@code LocalDate} and {@code LocalDateTime} instances are saved and parsed in a JSON.
 */
@Target({
        ElementType.RECORD_COMPONENT,
        ElementType.FIELD,
})
@Retention(RetentionPolicy.RUNTIME)
public @interface DateFormat {
    /**
     * The date/time/datetime string format.
     * @return format string.
     */
    String value();
}
