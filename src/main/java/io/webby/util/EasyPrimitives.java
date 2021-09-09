package io.webby.util;

import java.util.Arrays;

public class EasyPrimitives {
    public static class BoolFlag {
        public boolean flag = false;

        public BoolFlag() {
        }

        public BoolFlag(boolean flag) {
            this.flag = flag;
        }

        public boolean flag() {
            return flag;
        }
    }

    public static class IntCounter {
        public int value = 0;

        public IntCounter() {
        }

        public IntCounter(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }
    }

    public static class LongCounter {
        public long value = 0;

        public LongCounter() {
        }

        public LongCounter(int value) {
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
}
