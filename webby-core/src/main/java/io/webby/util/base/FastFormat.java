package io.webby.util.base;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FastFormat {
    public static @NotNull String format(@NotNull String pattern, @Nullable Object arg) {
        return pattern.replace("%s", String.valueOf(arg));
    }

    public static @NotNull String format(@NotNull String pattern, int arg) {
        return pattern.replace("%s", Integer.toString(arg));
    }

    public static @NotNull String format(@NotNull String pattern, long arg) {
        return pattern.replace("%s", Long.toString(arg));
    }

    public static @NotNull String format(@NotNull String pattern, boolean arg) {
        return pattern.replace("%s", Boolean.toString(arg));
    }

    public static @NotNull String format(@NotNull String pattern, @Nullable Object @NotNull ... args) {
        int total = 0;
        for (int i = 0; i < args.length; i++) {
            String s = String.valueOf(args[i]);
            args[i] = s;
            total += s.length();
        }

        StringBuilder builder = new StringBuilder(pattern.length() + total - 2 * args.length);
        int prevStart = 0;
        for (Object arg : args) {
            int newStart = pattern.indexOf("%s", prevStart);
            if (newStart == -1) {
                break;
            }
            builder.append(pattern, prevStart, newStart);
            builder.append(arg.toString());
            prevStart = newStart + 2;
        }
        builder.append(pattern, prevStart, pattern.length());
        return builder.toString();
    }
}
