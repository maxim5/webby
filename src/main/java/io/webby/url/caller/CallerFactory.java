package io.webby.url.caller;

import io.routekit.util.CharBuffer;
import io.webby.url.UrlConfigError;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

// TODO: handle default values
public class CallerFactory {
    @NotNull
    public Caller create(@NotNull Object instance, @NotNull Method method, @NotNull Annotation annotation, @NotNull List<String> vars) {
        Parameter[] parameters = method.getParameters();

        if (parameters.length == 0) {
            UrlConfigError.failIf(vars.size() > 0, "Incomplete method %s arguments: variables=%s".formatted(method, vars));
            return new EmptyCaller(instance, method);
        }

        if (parameters.length == 1) {
            UrlConfigError.failIf(vars.size() > 1, "Incomplete method %s arguments: variables=%s".formatted(method, vars));
            Class<?> type = parameters[0].getType();
            if (type.equals(int.class)) {
                return new IntCaller(instance, method);
            }
            if (type.equals(String.class)) {
                return new StringCaller(instance, method);
            }
            if (type.equals(CharBuffer.class)) {
                return new CharBufferCaller(instance, method);
            }
        }

        throw new UrlConfigError("Failed to recognize method signature: %s".formatted(method));
    }
}
