package io.webby.url.caller;

import com.google.common.collect.Iterables;
import io.routekit.util.CharBuffer;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Map;

public record IntCaller(Object instance, Method method) implements Caller {
    @Override
    public Object call(@NotNull CharSequence url, @NotNull Map<String, CharBuffer> variables) throws Exception {
        CharBuffer value = Iterables.getFirst(variables.values(), null);
        int intValue = Integer.parseInt(value, 0, value.length(), 10);
        return method.invoke(instance, intValue);
    }
}
