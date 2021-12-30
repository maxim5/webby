package io.webby.url.caller;

import io.netty.handler.codec.http.FullHttpRequest;
import io.routekit.util.CharArray;
import io.webby.url.convert.StringConverter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

public record StringCaller(Object instance, Method method, StringConverter validator, String name,
                           RichCallOptions opts) implements Caller {
    @Override
    public Object call(@NotNull FullHttpRequest request, @NotNull Map<String, CharArray> variables) throws Exception {
        CharSequence value = (opts.wantsBuffer1) ? variables.get(name) : Objects.toString(variables.get(name), null);
        validator.validateString(name, value);
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