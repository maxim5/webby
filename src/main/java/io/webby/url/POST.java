package io.webby.url;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface POST {
    String url();
    String contentType() default "";
    boolean jsonIn() default true;
    boolean jsonOut() default true;
}
