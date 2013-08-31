package uk.org.nickmoore.runtrack.database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation for fields that represents fields that connect classes to other classes, with an
 * optional alias prefix for SQL queries.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ForeignKey {
    String aliasPrefix() default "";
}
