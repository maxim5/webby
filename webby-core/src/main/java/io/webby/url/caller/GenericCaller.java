package io.webby.url.caller;

import io.netty.handler.codec.http.FullHttpRequest;
import io.spbx.util.base.CharArray;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public record GenericCaller(@NotNull Object instance, Method method, List<CallArgumentFunction> mapping) implements Caller {
    @Override
    public Object call(@NotNull FullHttpRequest request, @NotNull Map<String, CharArray> variables) throws Exception {
        Object[] args = mapping.stream().map(function -> function.apply(request, variables)).toArray();
        return method.invoke(instance, args);
    }
}
