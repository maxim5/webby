package io.webby.util.base;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class EasyPrimitives {
    public enum OptionalBool {
        TRUE,
        FALSE,
        UNKNOWN,
    }

    public static class MutableBool {
        public boolean flag = false;

        public MutableBool() {
        }

        public MutableBool(boolean flag) {
            this.flag = flag;
        }

        public boolean flag() {
            return flag;
        }
    }

    public static class MutableInt {
        public int value = 0;

        public MutableInt() {
        }

        public MutableInt(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }
    }

    public static class MutableLong {
        public long value = 0;

        public MutableLong() {
        }

        public MutableLong(int value) {
            this.value = value;
        }

        public long value() {
            return value;
        }
    }

    public static int firstNonNegative(int a, int b) {
        if (a >= 0) return a;
        if (b >= 0) return b;
        throw new IllegalArgumentException("All numbers are negative: %d, %d".formatted(a, b));
    }

    public static int firstNonNegative(int a, int b, int c) {
        if (a >= 0) return a;
        if (b >= 0) return b;
        if (c >= 0) return c;
        throw new IllegalArgumentException("All numbers are negative: %d, %d, %d".formatted(a, b, c));
    }

    public static int firstNonNegative(int ... nums) {
        for (int num : nums) {
            if (num >= 0) {
                return num;
            }
        }
        throw new IllegalArgumentException("All numbers are negative: %s".formatted(Arrays.toString(nums)));
    }

    public static int parseIntSafe(@Nullable CharSequence val, int def) {
        try {
            return val != null ? Integer.parseInt(val, 0, val.length(), 10) : def;
        } catch (NumberFormatException ignore) {
            return def;
        }
    }

    public static int parseIntSafe(@Nullable CharSequence val) {
        return parseIntSafe(val, 0);
    }
}
