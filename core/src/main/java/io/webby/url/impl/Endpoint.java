package io.webby.url.impl;

import io.webby.url.caller.Caller;

public record Endpoint(Caller caller, EndpointContext context, EndpointOptions options) {
}
