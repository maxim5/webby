package io.webby.url.handle;

import io.webby.netty.HttpRequestEx;
import org.jetbrains.annotations.NotNull;

public interface IntHandler<T> {
    T handleInt(@NotNull HttpRequestEx request, int value);
}
