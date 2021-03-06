package io.webby.url.handle;

import io.routekit.util.CharArray;
import io.webby.netty.request.HttpRequestEx;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface Handler<T> {
    T handle(@NotNull HttpRequestEx request, @NotNull Map<String, CharArray> variables);
}
