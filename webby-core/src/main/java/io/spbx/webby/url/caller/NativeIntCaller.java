package io.spbx.webby.url.caller;

import io.netty.handler.codec.http.FullHttpRequest;
import io.spbx.util.base.CharArray;
import io.spbx.webby.netty.request.HttpRequestEx;
import io.spbx.webby.url.convert.IntConverter;
import io.spbx.webby.url.handle.IntHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public record NativeIntCaller(IntHandler<?> handler, IntConverter validator, String name) implements Caller {
    @Override
    public Object call(@NotNull FullHttpRequest request, @NotNull Map<String, CharArray> variables) throws Exception {
        CharArray value = variables.get(name);
        int intValue = validator.validateInt(name, value);
        return handler.handleInt((HttpRequestEx) request, intValue);
    }

    @Override
    public @NotNull Object method() {
        return handler;
    }
}
