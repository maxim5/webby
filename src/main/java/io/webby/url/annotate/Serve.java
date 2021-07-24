package io.webby.url.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Serve {
    String url() default "";

    // Note: can not have more than 1 render
    Render[] render() default {};

    boolean disabled() default false;
}
