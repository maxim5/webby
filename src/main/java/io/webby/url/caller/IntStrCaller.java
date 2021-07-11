package io.webby.url.caller;

import io.netty.handler.codec.http.FullHttpRequest;
import io.routekit.util.CharBuffer;
import io.webby.url.validate.IntValidator;
import io.webby.url.validate.StringValidator;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Map;

public record IntStrCaller(Object instance, Method method,
                           IntValidator intValidator, StringValidator strValidator,
                           String intName, String strName,
                           RichCallOptions opts) implements Caller {
    @Override
    public Object call(@NotNull FullHttpRequest request, @NotNull Map<String, CharBuffer> variables) throws Exception {
        int intValue = intValidator.validateInt(intName, variables.get(intName));
        CharSequence strValue = (opts.wantsBuffer2) ? variables.get(strName) : variables.get(strName).toString();
        strValidator.validateString(strName, strValue);

        if (opts.wantsContent()) {
            Object content = opts.contentProvider.getContent(request);
            return opts.swapArgs ?
                    opts.wantsRequest ?
                            method.invoke(instance, request, strValue, intValue, content) :
                            method.invoke(instance, strValue, intValue, content) :
                    opts.wantsRequest ?
                            method.invoke(instance, request, intValue, strValue, content) :
                            method.invoke(instance, intValue, strValue, content);
        } else {
            return opts.swapArgs ?
                    opts.wantsRequest ?
                            method.invoke(instance, request, strValue, intValue) :
                            method.invoke(instance, strValue, intValue) :
                    opts.wantsRequest ?
                            method.invoke(instance, request, intValue, strValue) :
                            method.invoke(instance, intValue, strValue);
        }
    }
}
