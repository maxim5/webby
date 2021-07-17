package io.webby.url.impl;

import io.webby.url.validate.Validator;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public record EndpointContext(@NotNull Map<String, Validator> validators, boolean rawRequest) {
}
