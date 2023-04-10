package io.webby.orm.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents a page of items and an (optional) token for the next page.
 */
public record Page<E>(@NotNull List<E> items, @Nullable PageToken nextToken) {
    public boolean hasNextPage() {
        return nextToken != null;
    }
}
