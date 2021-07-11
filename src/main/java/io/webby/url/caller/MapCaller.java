package io.webby.url.caller;

import io.netty.handler.codec.http.FullHttpRequest;
import io.routekit.util.CharBuffer;
import io.webby.url.validate.Validator;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Map;

public record MapCaller(Object instance, Method method, Map<String, Validator> validators, CallOptions opts) implements Caller {
    @Override
    public Object call(@NotNull FullHttpRequest request, @NotNull Map<String, CharBuffer> variables) throws Exception {
        // TODO: call validators?
        if (opts.wantsContent()) {
            Object content = opts.contentProvider.getContent(request);
            return opts.wantsRequest ?
                    method.invoke(instance, request, variables, content) :
                    method.invoke(instance, variables, content);
        } else {
            return opts.wantsRequest ? method.invoke(instance, request, variables) : method.invoke(instance, variables);
        }
    }
}
