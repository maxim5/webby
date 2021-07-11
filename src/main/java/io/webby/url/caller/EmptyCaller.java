package io.webby.url.caller;

import io.netty.handler.codec.http.FullHttpRequest;
import io.routekit.util.CharBuffer;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Map;

public record EmptyCaller(Object instance, Method method, CallOptions opts) implements Caller {
    public Object call(@NotNull FullHttpRequest request, @NotNull Map<String, CharBuffer> variables) throws Exception {
        if (opts.wantsContent()) {
            Object content = opts.contentProvider.getContent(request);
            return opts.wantsRequest ? method.invoke(instance, request, content) : method.invoke(instance, content);
        } else {
            return opts.wantsRequest ? method.invoke(instance, request) : method.invoke(instance);
        }
    }
}
