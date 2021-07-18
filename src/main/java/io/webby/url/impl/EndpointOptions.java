package io.webby.url.impl;

import io.webby.url.Marshal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record EndpointOptions(@Nullable Marshal in, @NotNull Marshal out,
                              @NotNull CharSequence contentType,
                              boolean expectsContent) {
    public static final EndpointOptions DEFAULT = new EndpointOptions(null, Marshal.AS_STRING, "", false);
}
