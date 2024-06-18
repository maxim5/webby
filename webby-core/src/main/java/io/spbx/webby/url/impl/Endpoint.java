package io.spbx.webby.url.impl;

import io.spbx.webby.url.caller.Caller;

public record Endpoint(Caller caller, EndpointContext context, EndpointOptions options) {
}
