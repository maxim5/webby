package io.webby.util.base;

import org.checkerframework.dataflow.qual.Pure;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class EasyPrimitives {
    @Pure
    public static int firstNonNegative(int a, int b) {
        if (a >= 0) return a;
        if (b >= 0) return b;
        throw new IllegalArgumentException("All numbers are negative: %d, %d".formatted(a, b));
    }

    @Pure
    public static int firstNonNegative(int a, int b, int c) {
        if (a >= 0) return a;
        if (b >= 0) return b;
        if (c >= 0) return c;
        throw new IllegalArgumentException("All numbers are negative: %d, %d, %d".formatted(a, b, c));
    }

    @Pure
    public static int firstNonNegative(int ... nums) {
        for (int num : nums) {
            if (num >= 0) {
                return num;
            }
        }
        throw new IllegalArgumentException("All numbers are negative: %s".formatted(Arrays.toString(nums)));
    }

    @Pure
    public static int requirePositive(int val) {
        assert val > 0 : "Value must be positive: " + val;
        return val;
    }

    @Pure
    public static int requireNonNegative(int val) {
        assert val >= 0 : "Value must be non-negative: " + val;
        return val;
    }

    @Pure
    public static int parseIntSafe(@Nullable CharSequence val, int def) {
        try {
            return val != null ? Integer.parseInt(val, 0, val.length(), 10) : def;
        } catch (NumberFormatException ignore) {
            return def;
        }
    }

    @Pure
    public static int parseIntSafe(@Nullable CharSequence val) {
        return parseIntSafe(val, 0);
    }
}
