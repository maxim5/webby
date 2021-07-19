package io.webby.netty.request;

import io.netty.handler.codec.http.FullHttpRequest;
import org.jetbrains.annotations.NotNull;

public interface HttpRequestEx extends FullHttpRequest {
    @NotNull
    QueryParams params();
}
