package io.webby.url.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ServeWebsocket {
    String url();

    Class<?>[] messages() default {};

    boolean disabled() default false;
}
