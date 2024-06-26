package io.spbx.webby.url.caller;

import io.netty.handler.codec.http.FullHttpRequest;
import io.spbx.util.base.CharArray;
import io.spbx.webby.url.convert.IntConverter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Map;

public record IntIntCaller(Object instance, Method method,
                           IntConverter validator1, IntConverter validator2,
                           String name1, String name2,
                           CallOptions opts) implements Caller {
    @Override
    public Object call(@NotNull FullHttpRequest request, @NotNull Map<String, CharArray> variables) throws Exception {
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
