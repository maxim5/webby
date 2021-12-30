package io.webby.url.impl;

import io.webby.url.annotate.Marshal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record EndpointOptions(@Nullable Marshal in, @NotNull Marshal out,
                              @NotNull EndpointHttp http,
                              @Nullable EndpointView<?> view,
                              boolean expectsContent) {
    public static final EndpointOptions DEFAULT =
            new EndpointOptions(null, Marshal.AS_STRING, EndpointHttp.EMPTY, null, false);

    public boolean requiresAuth() {
        return false;
    }
}