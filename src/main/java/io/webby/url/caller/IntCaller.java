package io.webby.url.caller;

import io.routekit.util.CharBuffer;
import io.webby.url.validate.IntValidator;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Map;

public record IntCaller(Object instance, Method method, IntValidator validator, String name) implements Caller {
    @Override
    public Object call(@NotNull CharSequence url, @NotNull Map<String, CharBuffer> variables) throws Exception {
        CharBuffer value = variables.get(name);
        int intValue = validator.validateInt(name, value);
        return method.invoke(instance, intValue);
    }
}
