package io.webby.url.impl;

import java.lang.reflect.Method;

public record Binding(String url, Method method, String type, EndpointOptions options) {
}
