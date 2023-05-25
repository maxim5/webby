package io.webby.util.base;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FastFormat {
    public static @NotNull String format(@NotNull String pattern, @Nullable Object arg) {
        String s = String.valueOf(arg);
        return pattern.replace("%s", s).replace("%%", "%");
    }

    public static @NotNull String format(@NotNull String pattern, int arg) {
        String s = Integer.toString(arg);
        return pattern.replace("%s", s).replace("%%", "%");
    }

    public static @NotNull String format(@NotNull String pattern, long arg) {
        String s = Long.toString(arg);
        return pattern.replace("%s", s).replace("%%", "%");
    }

    public static @NotNull String format(@NotNull String pattern, boolean arg) {
        String s = Boolean.toString(arg);
        return pattern.replace("%s", s).replace("%%", "%");
    }

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
                prev = -1;
                continue;
            }

            String replace = null;
            if (prev == '%') {
                if (value == 's') {
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
                builder.ensureCapacity(len - i + replace.length());
                builder.append(replace);
            } else if (value != '%') {
                builder.append(value);
            }

            prev = value;
        }

        return builder.toString();
    }
}
