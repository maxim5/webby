package io.webby.orm.api.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface Sql {
    /**
     * Indicates the name to be used in SQL definition.
     */
    String value() default "";

    /**
     * Indicates whether this field corresponds to a PRIMARY KEY column (or a composite PRIMARY KEY)
     */
    boolean primary() default false;

    /**
     * Indicates whether this field corresponds to a UNIQUE column (or a composite UNIQUE)
     */
    boolean unique() default false;
}
