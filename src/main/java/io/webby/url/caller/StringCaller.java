package io.webby.url.caller;

import io.netty.handler.codec.http.FullHttpRequest;
import io.routekit.util.CharBuffer;
import io.webby.url.validate.StringValidator;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Map;

public record StringCaller(Object instance, Method method, StringValidator validator, String name,
                           RichCallOptions opts) implements Caller {
    @Override
    public Object call(@NotNull FullHttpRequest request, @NotNull Map<String, CharBuffer> variables) throws Exception {
        CharSequence value = (opts.wantsBuffer1) ? variables.get(name) : variables.get(name).toString();
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
