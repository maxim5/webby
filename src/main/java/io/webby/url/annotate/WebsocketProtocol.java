package io.webby.url.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface WebsocketProtocol {
    Class<?> messages();

    FrameType[] type() default {};

    Marshal[] marshal() default {};
}
