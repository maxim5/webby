package io.spbx.webby.url.handle;

import io.spbx.webby.netty.request.HttpRequestEx;
import org.jetbrains.annotations.NotNull;

public interface IntHandler<T> {
    T handleInt(@NotNull HttpRequestEx request, int value);
}
