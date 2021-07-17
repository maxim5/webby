package io.webby.url;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Serve {
    String url() default "";
    SerializeMethod defaultIn() default SerializeMethod.JSON;
    SerializeMethod defaultOut() default SerializeMethod.AS_STRING;
}
