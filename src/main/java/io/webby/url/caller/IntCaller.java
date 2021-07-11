package io.webby.url.caller;

import io.netty.handler.codec.http.FullHttpRequest;
import io.routekit.util.CharBuffer;
import io.webby.url.validate.IntValidator;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Map;

public record IntCaller(Object instance, Method method, IntValidator validator, String name, CallOptions opts) implements Caller {
    @Override
    public Object call(@NotNull FullHttpRequest request, @NotNull Map<String, CharBuffer> variables) throws Exception {
        CharBuffer value = variables.get(name);
        int intValue = validator.validateInt(name, value);
        if (opts.wantsContent()) {
            Object content = opts.contentProvider.getContent(request);
            return opts.wantsRequest ?
                    method.invoke(instance, request, intValue, content) :
                    method.invoke(instance, intValue, content);
        } else {
            return opts.wantsRequest ?
                    method.invoke(instance, request, intValue) :
                    method.invoke(instance, intValue);
        }
    }
}
