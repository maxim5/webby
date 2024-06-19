package io.spbx.webby.url.handle;

import io.spbx.webby.netty.request.HttpRequestEx;
import org.jetbrains.annotations.NotNull;

public interface StringHandler<T> {
    T handleString(@NotNull HttpRequestEx request, @NotNull CharSequence value);
}
