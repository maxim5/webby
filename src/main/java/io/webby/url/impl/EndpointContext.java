package io.webby.url.impl;

import io.webby.url.convert.Constraint;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public record EndpointContext(@NotNull Map<String, Constraint<?>> constraints, boolean isRawRequest, boolean isVoid) {
}
