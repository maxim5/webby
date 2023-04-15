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
     * Indicates whether this field corresponds to a PRIMARY KEY column.
     * In case of multiple columns, indicates a composite PRIMARY KEY.
     */
    boolean primary() default false;

    /**
     * Indicates whether this field corresponds to a UNIQUE column.
     * In case of multiple columns, indicates a composite UNIQUE.
     */
    boolean unique() default false;

    /**
     * Indicates whether this field corresponds to a NULL column.
     * In case of multiple columns, all of them are marked as NULL.
     */
    boolean nullable() default false;

    /**
     * Indicates the default value corresponding to the column's DEFAULT datatype definition.
     * In case of multiple columns, the size of an array must match the number of columns.
     */
    String[] defaults() default {};
}
