package io.spbx.util.testing;

import com.google.common.collect.Range;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;

public class TestingBigIntegers {
    public static final BigInteger $0 = BigInteger.ZERO;                // 0
    public static final BigInteger $1 = BigInteger.ONE;                 // 1
    public static final BigInteger $2 = BigInteger.TWO;                 // 2
    public static final BigInteger $10 = BigInteger.TEN;                // 10
    public static final BigInteger $2_8 = $pow2(8);                     // (1 << 8)
    public static final BigInteger $2_16 = $pow2(16);                   // (1 << 16)
    public static final BigInteger $2_31 = $pow2(31);                   // (1 << 31)
    public static final BigInteger $2_32 = $pow2(32);                   // (1 << 32)
    public static final BigInteger $2_63 = $pow2(63);                   // (1 << 63)
    public static final BigInteger $2_64 = $pow2(64);                   // (1 << 64)
    public static final BigInteger $2_127 = $pow2(127);                 // (1 << 127)
    public static final BigInteger $2_128 = $pow2(128);                 // (1 << 128)

    public static final BigInteger INT8_MAX = $(Byte.MAX_VALUE);        // (1 << 7) - 1
    public static final BigInteger INT8_MIN = $(Byte.MIN_VALUE);        //-(1 << 7)
    public static final BigInteger UINT8_MAX = $2_8.subtract($1);       // (1 << 8) - 1
    public static final BigInteger INT16_MAX = $(Short.MAX_VALUE);      // (1 << 15) - 1
    public static final BigInteger INT16_MIN = $(Short.MIN_VALUE);      //-(1 << 15)
    public static final BigInteger UINT16_MAX = $2_16.subtract($1);     // (1 << 16) - 1
    public static final BigInteger INT32_MAX = $(Integer.MAX_VALUE);    // (1 << 31) - 1
    public static final BigInteger INT32_MIN = $(Integer.MIN_VALUE);    //-(1 << 31)
    public static final BigInteger UINT32_MAX = $2_32.subtract($1);     // (1 << 32) - 1
    public static final BigInteger INT64_MAX = $(Long.MAX_VALUE);       // (1 << 63) - 1
    public static final BigInteger INT64_MIN = $(Long.MIN_VALUE);       //-(1 << 63)
    public static final BigInteger UINT64_MAX = $2_64.subtract($1);     // (1 << 64) - 1
    public static final BigInteger INT128_MAX = $2_127.subtract($1);    // (1 << 127) - 1
    public static final BigInteger INT128_MIN = $2_127.negate();        //-(1 << 127)
    public static final BigInteger UINT128_MAX = $2_128.subtract($1);   // (1 << 128) - 1

    public static final BigRange RANGE_INT8 = BigRange.rangeOf(INT8_MIN, INT8_MAX);
    public static final BigRange RANGE_UNT8 = BigRange.rangeOf($0, UINT8_MAX);
    public static final BigRange RANGE_INT16 = BigRange.rangeOf(INT16_MIN, INT16_MAX);
    public static final BigRange RANGE_UINT16 = BigRange.rangeOf($0, UINT16_MAX);
    public static final BigRange RANGE_INT32 = BigRange.rangeOf(INT32_MIN, INT32_MAX);
    public static final BigRange RANGE_UINT32 = BigRange.rangeOf($0, UINT32_MAX);
    public static final BigRange RANGE_INT64 = BigRange.rangeOf(INT64_MIN, INT64_MAX);
    public static final BigRange RANGE_UINT64 = BigRange.rangeOf($0, UINT64_MAX);
    public static final BigRange RANGE_INT128 = BigRange.rangeOf(INT128_MIN, INT128_MAX);
    public static final BigRange RANGE_UINT128 = BigRange.rangeOf($0, UINT128_MAX);

    public static @NotNull BigInteger $(long value) {
        return BigInteger.valueOf(value);
    }

    public static @NotNull BigInteger $(@NotNull String s) {
        return new BigInteger(s);
    }

    public static @NotNull BigInteger $pow2(int n) {
        return $1.shiftLeft(n);
    }

    public record BigRange(@NotNull Range<BigInteger> range) {
        public static @NotNull BigRange rangeOf(@NotNull BigInteger fromInclusive, @NotNull BigInteger toInclusive) {
            return new BigRange(Range.closed(fromInclusive, toInclusive));
        }

        public @NotNull BigInteger fitIn(@NotNull BigInteger value) {
            BigInteger result = range.contains(value) ? value : value.mod(total());
            return range.contains(result) ? result : result.subtract(total());
        }

        public boolean contains(@NotNull BigInteger value) {
            return range.contains(value);
        }

        public @NotNull BigInteger total() {
            return range.upperEndpoint().add($1).subtract(range.lowerEndpoint());
        }
    }
}
