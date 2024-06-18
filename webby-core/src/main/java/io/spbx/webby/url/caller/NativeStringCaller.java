package io.spbx.webby.url.caller;

import io.netty.handler.codec.http.FullHttpRequest;
import io.spbx.util.base.CharArray;
import io.spbx.webby.netty.request.HttpRequestEx;
import io.spbx.webby.url.convert.Constraint;
import io.spbx.webby.url.handle.StringHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public record NativeStringCaller(StringHandler<?> handler, Constraint<CharArray> validator, String name) implements Caller {
    @Override
    public Object call(@NotNull FullHttpRequest request, @NotNull Map<String, CharArray> variables) throws Exception {
        CharArray value = validator.applyWithName(name, variables.get(name));
        return handler.handleString((HttpRequestEx) request, value);
    }

    @Override
    public @NotNull Object method() {
        return handler;
    }
}
