package io.spbx.util.base;

import io.spbx.util.base.EasyExceptions.IllegalArgumentExceptions;
import org.checkerframework.dataflow.qual.Pure;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collector;

import static io.spbx.util.base.EasyExceptions.newIllegalArgumentException;

public class EasyPrimitives {
    @Pure
    public static int firstNonNegative(int a, int b) {
        if (a >= 0) return a;
        if (b >= 0) return b;
        throw newIllegalArgumentException("All numbers are negative: %d, %d", a, b);
    }

    @Pure
    public static int firstNonNegative(int a, int b, int c) {
        if (a >= 0) return a;
        if (b >= 0) return b;
        if (c >= 0) return c;
        throw newIllegalArgumentException("All numbers are negative: %d, %d, %d", a, b, c);
    }

    @Pure
    public static int firstNonNegative(int ... nums) {
        for (int num : nums) {
            if (num >= 0) {
                return num;
            }
        }
        throw newIllegalArgumentException("All numbers are negative: %s", Arrays.toString(nums));
    }

    @Pure
    public static int requirePositive(int val) {
        IllegalArgumentExceptions.assure(val > 0, "Value must be positive: %s", val);
        return val;
    }

    @Pure
    public static int requireNonNegative(int val) {
        IllegalArgumentExceptions.assure(val >= 0, "Value must be non-negative: %s", val);
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

    @Pure
    public static long parseLongSafe(@Nullable CharSequence val, long def) {
        try {
            return val != null ? Long.parseLong(val, 0, val.length(), 10) : def;
        } catch (NumberFormatException ignore) {
            return def;
        }
    }

    @Pure
    public static long parseLongSafe(@Nullable CharSequence val) {
        return parseLongSafe(val, 0);
    }

    @Pure
    public static byte parseByteSafe(@Nullable String val, byte def) {
        try {
            return val != null ? Byte.parseByte(val, 10) : def;
        } catch (NumberFormatException ignore) {
            return def;
        }
    }

    @Pure
    public static byte parseByteSafe(@Nullable String val) {
        return parseByteSafe(val, (byte) 0);
    }

    public static boolean parseBoolSafe(@Nullable String val, boolean def) {
        if (val != null) {
            if ("true".equalsIgnoreCase(val)) {
                return true;
            }
            if ("false".equalsIgnoreCase(val)) {
                return false;
            }
        }
        return def;
    }

    public static boolean parseBoolSafe(@Nullable String val) {
        return "true".equalsIgnoreCase(val);
    }

    @Pure
    public static double parseDoubleSafe(@Nullable String val, double def) {
        try {
            return val != null ? Double.parseDouble(val) : def;
        } catch (NumberFormatException ignore) {
            return def;
        }
    }

    @Pure
    public static double parseDoubleSafe(@Nullable String val) {
        return parseDoubleSafe(val, 0.0);
    }

    @Pure
    public static float parseFloatSafe(@Nullable String val, float def) {
        try {
            return val != null ? Float.parseFloat(val) : def;
        } catch (NumberFormatException ignore) {
            return def;
        }
    }

    @Pure
    public static float parseFloatSafe(@Nullable String val) {
        return parseFloatSafe(val, 0.0f);
    }

    public static @NotNull Collector<Integer, ?, byte[]> toByteArray() {
        // https://stackoverflow.com/questions/44708532/how-to-map-and-collect-primitive-return-type-using-java-8-stream
        return Collector.of(ByteArrayOutputStream::new, ByteArrayOutputStream::write, (baos1, baos2) -> {
            try {
                baos2.writeTo(baos1);
                return baos1;
            } catch (IOException e) {
                return Unchecked.rethrow(e);
            }
        }, ByteArrayOutputStream::toByteArray);
    }
}
