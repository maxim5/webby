package io.webby.url.handle;

import io.webby.netty.request.HttpRequestEx;
import org.jetbrains.annotations.NotNull;

public interface StringHandler<T> {
    T handleString(@NotNull HttpRequestEx request, @NotNull CharSequence value);
}
