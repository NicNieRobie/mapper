package dev.vpendischuk.mapper.json.annotations;

import dev.vpendischuk.mapper.json.annotations.enums.NullHandling;
import dev.vpendischuk.mapper.json.annotations.enums.UnknownPropertiesPolicy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used for denoting a type as a type that can be used for marshalling a JSON
 *   using its fields or records as entries.
 * <p>
 * Provides policies for marshalling and unmarshalling JSON models of annotated type:
 * <ul>
 *     <li>{@code nullHandling} - policy which denotes if null values should be excluded or included
 *                                   (default - {@code EXCLUDE});</li>
 *     <li>{@code unknownPropertiesPolicy} - policy which denotes if unknown values should be ignored
 *                                   when unmarshalling a JSON, or an exception must be thrown
 *                                   (default - {@code FAIL}).</li>
 * </ul>
 * <p>
 * Examples of usage:
 *
 * <pre>
 * // Default: excludes null values when marshalling, throws an exception on unknown value detection
 * &#64;Exported
 * class Foo1 { ... }
 *
 * &#64;Exported(nullHandling = NullHandling.INCLUDE, unknownPropertiesPolicy = UnknownPropertiesPolicy.IGNORE)
 * class Foo2 { ... }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Exported {
    NullHandling nullHandling() default NullHandling.EXCLUDE;
    UnknownPropertiesPolicy unknownPropertiesPolicy()
            default UnknownPropertiesPolicy.FAIL;
}