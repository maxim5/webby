package io.webby.util.base;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EasyWrappers {
    public enum OptionalBool {
        TRUE,
        FALSE,
        UNKNOWN;

        public static @NotNull OptionalBool from(boolean bool) {
            return bool ? TRUE : FALSE;
        }

        public static @NotNull OptionalBool fromNullable(@Nullable Boolean bool) {
            return bool == null ? UNKNOWN : from(bool);
        }
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

        public MutableLong(long value) {
            this.value = value;
        }

        public long value() {
            return value;
        }
    }
}
