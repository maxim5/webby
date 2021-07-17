package io.webby.url.impl;

import io.webby.url.SerializeMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record EndpointOptions(@NotNull CharSequence contentType,
                              @Nullable SerializeMethod in,
                              @NotNull SerializeMethod out) {
    public static final EndpointOptions DEFAULT = new EndpointOptions("", null, SerializeMethod.AS_STRING);

    public boolean wantsContent() {
        return in != null;
    }
}
