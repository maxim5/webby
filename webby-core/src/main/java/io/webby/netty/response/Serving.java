package io.webby.netty.response;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface Serving {
    boolean accept(@NotNull HttpMethod method);

    @NotNull HttpResponse serve(@NotNull String path, @NotNull HttpRequest request) throws IOException;
}
