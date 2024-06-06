package io.webby.util.base;

import com.google.errorprone.annotations.Immutable;
import org.checkerframework.dataflow.qual.Pure;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Immutable
public class FastFormat {
    private final String pattern;

    public FastFormat(@NotNull String pattern) {
        this.pattern = pattern;
    }

    public static @NotNull FastFormat ff(@NotNull String pattern) {
        return new FastFormat(pattern);
    }

    public @NotNull String formatted(@Nullable Object arg) {
        return format(pattern, arg);
    }

    public @NotNull String formatted(int arg) {
        return format(pattern, arg);
    }

    public @NotNull String formatted(long arg) {
        return format(pattern, arg);
    }

    public @NotNull String formatted(boolean arg) {
        return format(pattern, arg);
    }

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

    @Pure
    public static @NotNull String format(@NotNull String pattern, @Nullable Object @NotNull ... args) {
        StringBuilder builder = new StringBuilder(pattern.length());
        int prevStart = 0;
        for (Object arg : args) {
            int newStart = pattern.indexOf("%s", prevStart);
            if (newStart == -1) {
                break;
            }
            builder.append(pattern, prevStart, newStart);
            builder.append(arg);
            prevStart = newStart + 2;
        }
        builder.append(pattern, prevStart, pattern.length());
        return builder.toString();
    }
}
