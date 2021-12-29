package io.webby.url.impl;

import io.netty.handler.codec.http.HttpRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface RouteEndpoint {
    @Nullable Endpoint getAcceptedEndpointOrNull(@NotNull HttpRequest request);

    @NotNull String describe();
}
