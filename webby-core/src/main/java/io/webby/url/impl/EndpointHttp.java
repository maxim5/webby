package io.webby.url.impl;

import io.webby.util.base.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record EndpointHttp(@NotNull CharSequence contentType, @NotNull List<Pair<String, String>> headers) {
    public static final EndpointHttp EMPTY = new EndpointHttp("", List.of());

    public boolean hasContentType() {
        return !contentType.isEmpty();
    }

    public @NotNull EndpointHttp mergeWithDefault(@NotNull EndpointHttp http) {
        CharSequence newContentType = hasContentType() ? contentType : http.contentType();
        List<Pair<String, String>> newHeaders = new ArrayList<>(http.headers);
        headers.forEach(header -> {
            newHeaders.removeIf(existing -> existing.getKey().equalsIgnoreCase(header.getKey()));
            newHeaders.add(header);
        });
        return new EndpointHttp(newContentType, newHeaders);
    }
}
