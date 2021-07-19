package io.webby.url.caller;

import io.netty.handler.codec.http.FullHttpRequest;
import io.routekit.util.CharBuffer;
import io.webby.netty.HttpRequestEx;
import io.webby.url.handle.IntHandler;
import io.webby.url.validate.IntValidator;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public record NativeIntCaller(IntHandler<?> handler, IntValidator validator, String name) implements Caller {
    @Override
    public Object call(@NotNull FullHttpRequest request, @NotNull Map<String, CharBuffer> variables) throws Exception {
        CharBuffer value = variables.get(name);
        int intValue = validator.validateInt(name, value);
        return handler.handleInt((HttpRequestEx) request, intValue);
    }

    @Override
    public @NotNull Object method() {
        return handler;
    }
}
