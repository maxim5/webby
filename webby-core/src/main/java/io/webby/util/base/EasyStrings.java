package io.webby.util.base;

import com.google.mu.util.Optionals;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class EasyStrings {
    public static @NotNull String removePrefix(@NotNull String big, @NotNull String small) {
        if (big.startsWith(small)) {
            return big.substring(small.length());
        }
        return big;
    }

    public static @NotNull String removeSuffix(@NotNull String big, @NotNull String small) {
        if (big.endsWith(small)) {
            return big.substring(0, big.length() - small.length());
        }
        return big;
    }

    public static @NotNull String firstNotEmpty(@NotNull String first, @NotNull String second) {
        if (first.isEmpty()) {
            assert !second.isEmpty() : "Both input strings are empty";
            return second;
        }
        return first;
    }

    public static @NotNull Optional<String> ofNonEmpty(@Nullable String s) {
        return Optionals.optional(s != null && !s.isEmpty(), s);
    }
}
