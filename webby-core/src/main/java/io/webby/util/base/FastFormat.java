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
        return builder.toString().replace("%%", "%");
    }
}
