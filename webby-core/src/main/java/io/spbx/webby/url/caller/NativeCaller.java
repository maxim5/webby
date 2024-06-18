package io.spbx.webby.url.caller;

import io.netty.handler.codec.http.FullHttpRequest;
import io.spbx.util.base.CharArray;
import io.spbx.webby.netty.request.HttpRequestEx;
import io.spbx.webby.url.handle.Handler;
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
