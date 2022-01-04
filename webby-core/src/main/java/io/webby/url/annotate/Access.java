package io.webby.url.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Access {
    int value() default Public;

    // Constants correspond to `UserAccess`
    int Public = 0;
    int AuthUsersOnly = 1;
    int SuperAdminOnly = Short.MAX_VALUE;
}
