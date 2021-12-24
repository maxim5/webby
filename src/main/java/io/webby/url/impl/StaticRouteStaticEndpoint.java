package io.webby.url.impl;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.routekit.util.CharArray;
import io.webby.netty.response.StaticServing;
import io.webby.url.caller.Caller;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class StaticRouteStaticEndpoint implements RouteEndpoint {
    private final Endpoint endpoint;
    private final StaticServing serving;

    public StaticRouteStaticEndpoint(@NotNull String path, @NotNull StaticServing serving) {
        this.serving = serving;
        endpoint = new Endpoint(new StaticCaller(path), EndpointContext.EMPTY_CONTEXT, EndpointOptions.DEFAULT);
    }

    @Override
    public @Nullable Endpoint getAcceptedEndpointOrNull(@NotNull HttpRequest request) {
        return serving.accept(request.method()) ? endpoint : null;
    }

    @Override
    public @NotNull String describe() {
        return endpoint.caller().method().toString();
    }

    private class StaticCaller implements Caller {
        private final String path;

        private StaticCaller(@NotNull String path) {
            this.path = path;
        }

        @Override
        public Object call(@NotNull FullHttpRequest request, @NotNull Map<String, CharArray> variables) throws Exception {
            return serving.serve(path, request);
        }

        @Override
        public @NotNull Object method() {
            return "StaticCaller:" + path;
        }
    }
}