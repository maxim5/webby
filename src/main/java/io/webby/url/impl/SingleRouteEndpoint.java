package io.webby.url.impl;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.webby.url.caller.Caller;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

record SingleRouteEndpoint(@NotNull Endpoint endpoint, @Nullable HttpMethod httpMethod) implements RouteEndpoint {
    @Override
    @Nullable
    public Endpoint getAcceptedEndpointOrNull(@NotNull HttpRequest request) {
        return (httpMethod == null || httpMethod.equals(request.method())) ? endpoint : null;
    }

    @Override
    @NotNull
    public String describe() {
        return "%s %s".formatted(httpMethod != null ? httpMethod.name() : "<ANY>", endpoint.caller().method());
    }

    @NotNull
    /*package*/ static SingleRouteEndpoint fromBinding(@NotNull Binding binding,
                                                       @NotNull Caller caller,
                                                       @NotNull EndpointContext context) {
        return new SingleRouteEndpoint(
                new Endpoint(caller, context, binding.options()),
                HttpMethod.valueOf(binding.type())
        );
    }
}
