package io.webby.url.impl;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.routekit.util.CharArray;
import io.webby.netty.response.Serving;
import io.webby.url.caller.Caller;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class DynamicServingRouteEndpoint implements RouteEndpoint {
    private final Endpoint endpoint;
    private final Serving serving;
    private final String prefix;

    public DynamicServingRouteEndpoint(@NotNull String prefix, @NotNull Serving serving) {
        this.serving = serving;
        this.prefix = prefix;
        this.endpoint = new Endpoint(new ServingCaller(), EndpointContext.EMPTY_CONTEXT, EndpointOptions.DEFAULT);
    }

    @Override
    public @Nullable Endpoint getAcceptedEndpointOrNull(@NotNull HttpRequest request) {
        return serving.accept(request.method()) ? endpoint : null;
    }

    @Override
    public @NotNull String describe() {
        return endpoint.caller().method().toString();
    }

    private class ServingCaller implements Caller {
        @Override
        public Object call(@NotNull FullHttpRequest request, @NotNull Map<String, CharArray> variables) throws Exception {
            CharArray path = variables.get("path");
            return serving.serve(path.toString(), request);
        }

        @Override
        public @NotNull Object method() {
            return "ServingCaller[%s->%s]".formatted(prefix, serving);
        }
    }
}