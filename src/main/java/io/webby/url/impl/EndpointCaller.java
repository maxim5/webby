package io.webby.url.impl;

import io.webby.url.caller.Caller;

public record EndpointCaller(Caller caller, EndpointContext context, EndpointOptions options) {
}
