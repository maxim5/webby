package io.webby.url.impl;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.webby.url.UrlConfigError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

record MultiRouteEndpoint(@NotNull Map<HttpMethod, SingleRouteEndpoint> map,
                          @Nullable SingleRouteEndpoint def) implements RouteEndpoint {
    @Override
    @Nullable
    public EndpointCaller getAcceptedCallerOrNull(@NotNull HttpRequest request) {
        SingleRouteEndpoint endpoint = map.getOrDefault(request.method(), def);
        return endpoint != null ? endpoint.caller() : null;
    }

    @Override
    @NotNull
    public String describe() {
        StringBuilder builder = new StringBuilder("\n");
        for (SingleRouteEndpoint value : map.values()) {
            builder.append(" ").append(value.describe()).append("\n");
        }
        return builder.toString();
    }

    /*package*/ static MultiRouteEndpoint fromEndpoints(@NotNull List<SingleRouteEndpoint> endpoints) {
        Map<HttpMethod, SingleRouteEndpoint> map = endpoints.stream().collect(Collectors.groupingBy(
                SingleRouteEndpoint::httpMethod,
                Collectors.reducing(null, (e1, e2) -> {
                    if (e1 == null) {
                        return e2;
                    }
                    throw new UrlConfigError(
                        "Multiple endpoints on the same URL and HTTP method: %s and %s".formatted(e1.describe(), e2.describe())
                    );
                })
        ));
        return new MultiRouteEndpoint(map, map.get(null));
    }
}
