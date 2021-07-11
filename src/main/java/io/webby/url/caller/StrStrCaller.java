package io.webby.url.caller;

import io.netty.handler.codec.http.FullHttpRequest;
import io.routekit.util.CharBuffer;
import io.webby.url.validate.StringValidator;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Map;

public record StrStrCaller(Object instance, Method method,
                           StringValidator validator1, StringValidator validator2,
                           String name1, String name2,
                           RichCallOptions opts) implements Caller {
    @Override
    public Object call(@NotNull FullHttpRequest request, @NotNull Map<String, CharBuffer> variables) throws Exception {
        CharSequence value1 = (opts.wantsBuffer1) ? variables.get(name1) : variables.get(name1).toString();
        validator1.validateString(name1, value1);

        CharSequence value2 = (opts.wantsBuffer2) ? variables.get(name2) : variables.get(name2).toString();
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
