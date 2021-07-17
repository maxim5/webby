package io.webby.url.impl;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.webby.url.caller.Caller;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

record SingleRouteEndpoint(@NotNull EndpointCaller caller, @Nullable HttpMethod httpMethod) implements RouteEndpoint {
    @Override
    @Nullable
    public EndpointCaller getAcceptedCallerOrNull(@NotNull HttpRequest request) {
        return (httpMethod == null || httpMethod.equals(request.method())) ? caller : null;
    }

    @Override
    @NotNull
    public String describe() {
        return "%s %s".formatted(httpMethod != null ? httpMethod.name() : "<ANY>", caller.caller().method());
    }

    @NotNull
    /*package*/ static SingleRouteEndpoint fromBinding(@NotNull Binding binding,
                                                       @NotNull Caller caller,
                                                       @NotNull EndpointContext context) {
        return new SingleRouteEndpoint(
                new EndpointCaller(caller, context, binding.options()),
                HttpMethod.valueOf(binding.type())
        );
    }
}
