package io.webby.util.base;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FastFormat {
    public static @NotNull String format(@NotNull String pattern, @Nullable Object @NotNull ... args) {
        StringBuilder builder = new StringBuilder(pattern.length());  // better estimate?

        int prev = -1;
        int argCounter = 0;
        boolean skipOne = false;

        for (int i = 0, len = pattern.length(); i < len; i++) {
            char value = pattern.charAt(i);

            if (skipOne) {
                skipOne = false;
                builder.append(value);
                prev = value;
                continue;
            }

            boolean isPercent = prev == '%';
            String replace = null;
            if (isPercent) {
                if (value == 's' || value == 'd') {
                    assert argCounter < args.length : "Invalid args: pattern=%s args=%s".formatted(pattern, args);
                    replace = String.valueOf(args[argCounter++]);
                } else if (value == '%') {
                    replace = "%";
                    skipOne = true;
                } else {
                    throw new IllegalArgumentException("Unrecognized pattern: " + pattern);
                }
            }

            if (replace != null) {
                builder.append(replace);
            } else if (value != '%') {
                builder.append(value);
            }

            prev = value;
        }

        return builder.toString();
    }
}
