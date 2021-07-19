package io.webby.url.caller;

import io.netty.handler.codec.http.FullHttpRequest;
import io.routekit.util.CharBuffer;
import io.webby.netty.HttpRequestEx;
import io.webby.url.handle.StringHandler;
import io.webby.url.validate.StringValidator;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public record NativeStringCaller(StringHandler<?> handler, StringValidator validator, String name) implements Caller {
    @Override
    public Object call(@NotNull FullHttpRequest request, @NotNull Map<String, CharBuffer> variables) throws Exception {
        CharBuffer value = variables.get(name);
        validator.validateString(name, value);
        return handler.handleString((HttpRequestEx) request, value);
    }

    @Override
    public @NotNull Object method() {
        return handler;
    }
}
