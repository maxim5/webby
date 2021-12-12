package io.webby.orm.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

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
        return token.startsWith(":") ? parseOffset(token) : new PageToken(token, NO_OFFSET);
    }

    public static @Nullable PageToken deserializeHumanReadableOrNull(@NotNull String token,
                                                                     @NotNull Preference preference) {
        return switch (preference) {
            case PREFER_LAST_ITEM -> new PageToken(token, NO_OFFSET);
            case PREFER_OFFSET -> token.startsWith(":") ? requireNonNull(parseOffset(token)) : null;
        };
    }

    public static Optional<PageToken> deserializeHumanReadable(@NotNull String token) {
        return Optional.ofNullable(deserializeHumanReadableOrNull(token));
    }

    public static Optional<PageToken> deserializeHumanReadable(@NotNull String token, @NotNull Preference preference) {
        return Optional.ofNullable(deserializeHumanReadableOrNull(token, preference));
    }

    private static @Nullable PageToken parseOffset(@NotNull String token) {
        try {
            int offset = Integer.parseInt(token.substring(1));
            return new PageToken(null, offset);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    enum Preference {
        PREFER_LAST_ITEM,
        PREFER_OFFSET
    }
}
