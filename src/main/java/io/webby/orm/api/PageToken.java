package io.webby.orm.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record PageToken(@Nullable String lastItem, int offset) {
    public static final int NO_OFFSET = -1;

    public boolean hasLastItem() {
        return lastItem != null;
    }

    public boolean hasOffset() {
        return offset != NO_OFFSET;
    }

    public @NotNull String serializeHumanReadable(@NotNull Preference preference) {
        if (lastItem != null && preference == Preference.PREFER_LAST_ITEM) {
            return lastItem;
        }
        return ":" + offset;
    }

    public static @Nullable PageToken deserializeHumanReadableOrNull(@NotNull String token) {
        if (token.startsWith(":")) {
            try {
                int offset = Integer.parseInt(token.substring(1));
                return new PageToken(null, offset);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return new PageToken(token, NO_OFFSET);
    }

    public static @NotNull Optional<PageToken> deserializeHumanReadable(@NotNull String token) {
        return Optional.ofNullable(deserializeHumanReadableOrNull(token));
    }

    enum Preference {
        PREFER_LAST_ITEM,
        PREFER_OFFSET
    }
}
