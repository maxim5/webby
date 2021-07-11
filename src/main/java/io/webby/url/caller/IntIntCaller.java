package io.webby.url.caller;

import io.netty.handler.codec.http.FullHttpRequest;
import io.routekit.util.CharBuffer;
import io.webby.url.validate.IntValidator;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Map;

public record IntIntCaller(Object instance, Method method,
                           IntValidator validator1, IntValidator validator2,
                           String name1, String name2,
                           CallOptions opts) implements Caller {
    @Override
    public Object call(@NotNull FullHttpRequest request, @NotNull Map<String, CharBuffer> variables) throws Exception {
        int intValue1 = validator1.validateInt(name1, variables.get(name1));
        int intValue2 = validator2.validateInt(name2, variables.get(name2));

        if (opts.wantsContent()) {
            Object content = opts.contentProvider.getContent(request);
            return opts.wantsRequest ?
                    method.invoke(instance, request, intValue1, intValue2, content) :
                    method.invoke(instance, intValue1, intValue2, content);
        } else {
            return opts.wantsRequest ?
                    method.invoke(instance, request, intValue1, intValue2) :
                    method.invoke(instance, intValue1, intValue2);
        }
    }
}
