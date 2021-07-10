package io.webby.url.caller;

import io.routekit.util.CharBuffer;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Map;

public record MapCaller(Object instance, Method method) implements Caller {
    @Override
    public Object call(@NotNull CharSequence url, @NotNull Map<String, CharBuffer> variables) throws Exception {
        return method.invoke(instance, variables);
    }
}
