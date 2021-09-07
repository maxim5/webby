package io.webby.util;

public class PrimitiveWrappers {
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
}
