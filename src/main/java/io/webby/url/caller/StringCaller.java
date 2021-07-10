package io.webby.url.caller;

import io.netty.handler.codec.http.FullHttpRequest;
import io.routekit.util.CharBuffer;
import io.webby.url.validate.StringValidator;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Map;

public record StringCaller(Object instance, Method method, StringValidator validator, String name,
                           boolean wantsRequest, boolean wantsBuffer) implements Caller {
    @Override
    public Object call(@NotNull FullHttpRequest request, @NotNull Map<String, CharBuffer> variables) throws Exception {
        CharSequence value = (wantsBuffer) ? variables.get(name) : variables.get(name).toString();
        validator.validateString(name, value);
        return wantsRequest ? method.invoke(instance, request, value) : method.invoke(instance, value);
    }
}
