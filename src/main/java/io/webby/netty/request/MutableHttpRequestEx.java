package io.webby.netty.request;

import org.jetbrains.annotations.NotNull;

public interface MutableHttpRequestEx extends HttpRequestEx {
    void setAttr(int position, @NotNull Object attr);
}
