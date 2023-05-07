package io.webby.netty.request;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface MutableHttpRequestEx extends HttpRequestEx {
    /**
     * Sets the non-null attribute {@code attr} at a given {@code position}.
     * Must be called no more than once per position. Throws if the value is already set.
     *
     * @see #attr(int)
     */
    void setAttr(int position, @NotNull Object attr);

    /**
     * Sets the nullable attribute {@code attr} at a given {@code position}.
     * Must be called no more than once per position. Throws if the value is already set.
     *
     * @see #attr(int)
     */
    void setNullableAttr(int position, @Nullable Object attr);
}
