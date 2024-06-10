package io.webby.url.caller;

import io.netty.handler.codec.http.FullHttpRequest;
import io.webby.util.base.CharArray;
import io.webby.url.convert.Constraint;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Map;

public record StrStrCaller(Object instance, Method method,
                           Constraint<? extends CharSequence> validator1, Constraint<? extends CharSequence> validator2,
                           String name1, String name2,
                           CallOptions opts) implements Caller {
    @Override
    public Object call(@NotNull FullHttpRequest request, @NotNull Map<String, CharArray> variables) throws Exception {
        CharSequence value1 = validator1.applyWithName(name1, variables.get(name1));
        CharSequence value2 = validator2.applyWithName(name2, variables.get(name2));

        if (opts.wantsContent()) {
            Object content = opts.contentProvider.getContent(request);
            return opts.wantsRequest ?
                    method.invoke(instance, request, value1, value2, content) :
                    method.invoke(instance, value1, value2, content);
        } else {
            return opts.wantsRequest ?
                    method.invoke(instance, request, value1, value2) :
                    method.invoke(instance, value1, value2);
        }
    }
}
