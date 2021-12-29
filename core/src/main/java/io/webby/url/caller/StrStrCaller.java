package io.webby.url.caller;

import io.netty.handler.codec.http.FullHttpRequest;
import io.routekit.util.CharArray;
import io.webby.url.convert.StringConverter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

public record StrStrCaller(Object instance, Method method,
                           StringConverter validator1, StringConverter validator2,
                           String name1, String name2,
                           RichCallOptions opts) implements Caller {
    @Override
    public Object call(@NotNull FullHttpRequest request, @NotNull Map<String, CharArray> variables) throws Exception {
        CharSequence value1 = (opts.wantsBuffer1) ? variables.get(name1) : Objects.toString(variables.get(name1), null);
        validator1.validateString(name1, value1);

        CharSequence value2 = (opts.wantsBuffer2) ? variables.get(name2) : Objects.toString(variables.get(name2), null);
        validator2.validateString(name2, value2);

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
