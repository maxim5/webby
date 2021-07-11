package io.webby.url.impl;

import io.webby.url.SerializeMethod;
import org.jetbrains.annotations.Nullable;

public record EndpointOptions(CharSequence contentType,
                              @Nullable SerializeMethod in,
                              SerializeMethod out) {
    boolean wantsContent() {
        return in != null;
    }
}
