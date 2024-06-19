package io.spbx.webby.url.handle;

import io.spbx.util.base.CharArray;
import io.spbx.webby.netty.request.HttpRequestEx;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface Handler<T> {
    T handle(@NotNull HttpRequestEx request, @NotNull Map<String, CharArray> variables);
}
