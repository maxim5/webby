package io.spbx.util.base;

import org.checkerframework.dataflow.qual.Pure;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class EasyStrings {
    @Pure
    public static @NotNull String removePrefix(@NotNull String big, @NotNull String small) {
        if (big.startsWith(small)) {
            return big.substring(small.length());
        }
        return big;
    }

    @Pure
    public static @NotNull String removeSuffix(@NotNull String big, @NotNull String small) {
        if (big.endsWith(small)) {
            return big.substring(0, big.length() - small.length());
        }
        return big;
    }

    @Pure
    public static @NotNull String ensurePrefix(@NotNull String big, @NotNull String small) {
        if (big.startsWith(small)) {
            return big;
        }
        return small + big;
    }

    @Pure
    public static @NotNull String ensureSuffix(@NotNull String big, @NotNull String small) {
        if (big.endsWith(small)) {
            return big;
        }
        return big + small;
    }

    @Pure
    public static @NotNull String firstNotEmpty(@NotNull String first, @NotNull String second) {
        if (first.isEmpty()) {
            assert !second.isEmpty() : "Both input strings are empty";
            return second;
        }
        return first;
    }

    @Pure
    public static @NotNull Optional<String> ofNonEmpty(@Nullable String s) {
        return s != null && !s.isEmpty() ? Optional.of(s) : Optional.empty();
    }
}
