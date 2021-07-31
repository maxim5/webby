package io.webby.netty.request;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface MutableHttpRequestEx extends HttpRequestEx {
    void setAttr(int position, @NotNull Object attr);

    void setNullableAttr(int position, @Nullable Object attr);
}
