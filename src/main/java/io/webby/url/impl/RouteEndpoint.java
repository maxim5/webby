package io.webby.url.impl;

import io.netty.handler.codec.http.HttpRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface RouteEndpoint {
    @Nullable
    EndpointCaller getAcceptedCallerOrNull(@NotNull HttpRequest request);
}
