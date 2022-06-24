package dev.vpendischuk.mapper.json.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to denote that a field or a record component
 *   which serves as a target of the annotation must be ignored
 *   during JSON marshalling.
 */
@Target({
        ElementType.RECORD_COMPONENT,
        ElementType.FIELD,
})
@Retention(RetentionPolicy.RUNTIME)
public @interface Ignored {
}
