package io.webby.url.caller;

import io.netty.handler.codec.http.FullHttpRequest;
import io.webby.util.base.CharArray;
import io.webby.netty.request.HttpRequestEx;
import io.webby.url.handle.Handler;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public record NativeCaller(@NotNull Handler<?> handler) implements Caller {
    @Override
    public Object call(@NotNull FullHttpRequest request, @NotNull Map<String, CharArray> variables) throws Exception {
        return handler.handle((HttpRequestEx) request, variables);
    }

    @Override
    public @NotNull Object method() {
        return handler;
    }
}
