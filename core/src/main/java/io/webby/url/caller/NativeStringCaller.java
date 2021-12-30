package io.webby.url.caller;

import io.netty.handler.codec.http.FullHttpRequest;
import io.routekit.util.CharArray;
import io.webby.netty.request.HttpRequestEx;
import io.webby.url.handle.StringHandler;
import io.webby.url.convert.StringConverter;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public record NativeStringCaller(StringHandler<?> handler, StringConverter validator, String name) implements Caller {
    @Override
    public Object call(@NotNull FullHttpRequest request, @NotNull Map<String, CharArray> variables) throws Exception {
        CharArray value = variables.get(name);
        validator.validateString(name, value);
        return handler.handleString((HttpRequestEx) request, value);
    }

    @Override
    public @NotNull Object method() {
        return handler;
    }
}