package io.webby.orm.api.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Model {
    String javaName() default "";

    String sqlName() default "";

    String javaTableName() default "";

    Class<?> exposeAs() default Void.class;
}
