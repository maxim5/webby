package io.spbx.webby.url.impl;

import io.spbx.webby.url.convert.Constraint;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;

public record EndpointContext(@NotNull Map<String, Constraint<?>> constraints, boolean bypassInterceptors, boolean isVoid) {
    public static final EndpointContext EMPTY_CONTEXT = new EndpointContext(Collections.emptyMap(), true, false);
}
