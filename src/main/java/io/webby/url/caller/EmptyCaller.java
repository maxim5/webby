package io.webby.url.caller;

import io.netty.handler.codec.http.FullHttpRequest;
import io.routekit.util.CharBuffer;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Map;

public record EmptyCaller(Object instance, Method method, boolean wantsRequest) implements Caller {
    public Object call(@NotNull FullHttpRequest request, @NotNull Map<String, CharBuffer> variables) throws Exception {
        return wantsRequest ? method.invoke(instance, request) : method.invoke(instance);
    }
}
