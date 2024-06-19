package io.spbx.webby.url.impl;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.spbx.util.base.CharArray;
import io.spbx.webby.netty.response.Serving;
import io.spbx.webby.url.caller.Caller;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class DynamicServingRouteEndpoint implements RouteEndpoint {
    private final Endpoint endpoint;
    private final Serving serving;
    private final String prefix;

    public DynamicServingRouteEndpoint(@NotNull String prefix, @NotNull String directory, @NotNull Serving serving) {
        this.serving = serving;
        this.prefix = prefix;
        this.endpoint = new Endpoint(new ServingCaller(directory), EndpointContext.EMPTY_CONTEXT, EndpointOptions.DEFAULT);
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
        private final String directory;

        public ServingCaller(@NotNull String directory) {
            this.directory = directory;
        }

        @Override
        public Object call(@NotNull FullHttpRequest request, @NotNull Map<String, CharArray> variables) throws Exception {
            CharArray path = variables.get("path");
            return serving.serve(UrlFix.joinWithSlash(directory, path), request);
        }

        @Override
        public @NotNull Object method() {
            return "ServingCaller[%s/%s/*->%s]".formatted(prefix, directory, serving);
        }
    }
}
