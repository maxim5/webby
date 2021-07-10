package io.webby.url.caller;

import io.netty.handler.codec.http.FullHttpRequest;
import io.routekit.util.CharBuffer;
import io.webby.url.validate.Validator;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Map;

public record MapCaller(Object instance, Method method, @NotNull Map<String, Validator> validators) implements Caller {
    @Override
    public Object call(@NotNull FullHttpRequest request, @NotNull Map<String, CharBuffer> variables) throws Exception {
        // TODO: call validators?
        return method.invoke(instance, variables);
    }
}
