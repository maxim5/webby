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
                           boolean wantsRequest, boolean wantsBuffer, boolean swapArgs) implements Caller {
    @Override
    public Object call(@NotNull FullHttpRequest request, @NotNull Map<String, CharBuffer> variables) throws Exception {
        int intValue = intValidator.validateInt(intName, variables.get(intName));
        CharSequence strValue = (wantsBuffer) ? variables.get(strName) : variables.get(strName).toString();
        strValidator.validateString(strName, strValue);

        return swapArgs ?
                wantsRequest ?
                        method.invoke(instance, request, strValue, intValue) :
                        method.invoke(instance, strValue, intValue) :
                wantsRequest ?
                        method.invoke(instance, request, intValue, strValue) :
                        method.invoke(instance, intValue, strValue);
    }
}
