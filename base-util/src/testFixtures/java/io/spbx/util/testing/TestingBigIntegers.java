package io.spbx.util.testing;

import com.google.common.primitives.UnsignedLong;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;

public class TestingBigIntegers {
    public static final BigInteger $0 = BigInteger.ZERO;                // 0
    public static final BigInteger $1 = BigInteger.ONE;                 // 1
    public static final BigInteger $2 = BigInteger.TWO;                 // 2
    public static final BigInteger $10 = BigInteger.TEN;                // 10
    public static final BigInteger $2_8 = $(1L << 8);                   // (1 << 8)
    public static final BigInteger $2_16 = $(1L << 16);                 // (1 << 16)
    public static final BigInteger $2_31 = $(1L << 31);                 // (1 << 31)
    public static final BigInteger $2_32 = $(1L << 32);                 // (1 << 32)
    public static final BigInteger $2_63 = $1.shiftLeft(63);            // (1 << 63)
    public static final BigInteger $2_64 = $1.shiftLeft(64);            // (1 << 64)
    public static final BigInteger $2_127 = $1.shiftLeft(127);          // (1 << 127)
    public static final BigInteger $2_128 = $1.shiftLeft(128);          // (1 << 128)

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

    public static @NotNull BigInteger $(long value) {
        return BigInteger.valueOf(value);
    }

    public static boolean isFitsIntoSignedLong(@NotNull BigInteger bigInteger) {
        return INT64_MAX.compareTo(bigInteger) >= 0 && INT64_MIN.compareTo(bigInteger) <= 0;
    }

    public static boolean isFitsIntoSigned128Bit(@NotNull BigInteger bigInteger) {
        return INT128_MAX.compareTo(bigInteger) >= 0 && INT128_MIN.compareTo(bigInteger) <= 0;
    }

    public static @NotNull BigInteger fitIntoSigned128Bits(@NotNull BigInteger bigInteger) {
        BigInteger result =
            bigInteger.compareTo(INT128_MAX) > 0 || bigInteger.compareTo(INT128_MIN) < 0 ?
                bigInteger.mod($2_128) :
                bigInteger;
        return result.compareTo($2_127) >= 0 ? result.subtract($2_128) : result;
    }

    public static @NotNull BigInteger toBigInteger(long highBits, long lowBits) {
        return $(highBits).shiftLeft(64).add(UnsignedLong.fromLongBits(lowBits).bigIntegerValue());
    }
}
