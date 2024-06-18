package io.spbx.webby.url.caller;

import io.netty.handler.codec.http.FullHttpRequest;
import io.spbx.util.base.CharArray;
import io.spbx.webby.url.convert.Constraint;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Map;

public record StringCaller(Object instance, Method method, Constraint<? extends CharSequence> validator, String name,
                           CallOptions opts) implements Caller {
    @Override
    public Object call(@NotNull FullHttpRequest request, @NotNull Map<String, CharArray> variables) throws Exception {
        CharSequence value = validator.applyWithName(name, variables.get(name));
        if (opts.wantsContent()) {
            Object content = opts.contentProvider.getContent(request);
            return opts.wantsRequest ?
                    method.invoke(instance, request, value, content) :
                    method.invoke(instance, value, content);
        } else {
            return opts.wantsRequest ?
                    method.invoke(instance, request, value) :
                    method.invoke(instance, value);
        }
    }
}
