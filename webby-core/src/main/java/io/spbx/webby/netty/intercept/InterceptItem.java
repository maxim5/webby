package io.spbx.webby.netty.intercept;

import org.jetbrains.annotations.NotNull;

public record InterceptItem(@NotNull Interceptor instance, boolean isOwner, int position, boolean canBeDisabled) {
}
