package io.webby.url.handle;

import io.routekit.util.CharBuffer;
import io.webby.netty.HttpRequestEx;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface Handler<T> {
    T handle(@NotNull HttpRequestEx request, @NotNull Map<String, CharBuffer> variables);
}
