package io.webby.url.caller;

import com.google.mu.util.stream.BiStream;
import io.netty.handler.codec.http.FullHttpRequest;
import io.spbx.util.base.CharArray;
import io.webby.url.convert.Constraint;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Map;

public record MapCaller(Object instance, Method method,
                        Map<String, Constraint<?>> constraints,
                        CallOptions opts) implements Caller {
    private static final Constraint<CharArray> identity = value -> value;

    @Override
    public Object call(@NotNull FullHttpRequest request, @NotNull Map<String, CharArray> variables) throws Exception {
        Map<String, ?> converted = convert(variables);
        if (opts.wantsContent()) {
            Object content = opts.contentProvider.getContent(request);
            return opts.wantsRequest ?
                    method.invoke(instance, request, converted, content) :
                    method.invoke(instance, converted, content);
        } else {
            return opts.wantsRequest ? method.invoke(instance, request, converted) : method.invoke(instance, converted);
        }
    }

    private @NotNull Map<String, ?> convert(@NotNull Map<String, CharArray> variables) {
        return BiStream.from(variables.entrySet())
                .mapValues((key, value) -> constraints.getOrDefault(key, identity).applyWithName(key, value))
                .toMap();
    }
}
