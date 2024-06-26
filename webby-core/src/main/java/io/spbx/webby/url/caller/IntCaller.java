package io.spbx.webby.url.caller;

import io.netty.handler.codec.http.FullHttpRequest;
import io.spbx.util.base.CharArray;
import io.spbx.webby.url.convert.IntConverter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Map;

public record IntCaller(Object instance, Method method, IntConverter validator, String name, CallOptions opts) implements Caller {
    @Override
    public Object call(@NotNull FullHttpRequest request, @NotNull Map<String, CharArray> variables) throws Exception {
        CharArray value = variables.get(name);
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
