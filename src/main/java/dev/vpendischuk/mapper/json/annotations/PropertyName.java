package dev.vpendischuk.mapper.json.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to change the name of a field or a record component
 *   to the specified {@code value} name during the JSON marshalling
 *   and unmarshalling.
 */
@Target({
        ElementType.RECORD_COMPONENT,
        ElementType.FIELD,
})
@Retention(RetentionPolicy.RUNTIME)
public @interface PropertyName {
    /**
     * Name of the field/component used by the mapper in (un)marshalling.
     * @return name string.
     */
    String value();
}
