package io.spbx.util.testing;

import com.google.common.primitives.UnsignedLong;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;

public class TestingPrimitives {
    public static int[] ints(int... values) {
        return values;
    }

    public static long[] longs(long... values) {
        return values;
    }

    public static boolean fitsIntoLong(@NotNull BigInteger bigInteger) {
        return BigInteger.valueOf(Long.MAX_VALUE).compareTo(bigInteger) >= 0 &&
               BigInteger.valueOf(Long.MIN_VALUE).compareTo(bigInteger) <= 0;
    }

    public static @NotNull BigInteger fitInto128Bits(@NotNull BigInteger bigInteger) {
        BigInteger mod = BigInteger.ONE.shiftLeft(128);
        BigInteger max = BigInteger.ONE.shiftLeft(127);
        BigInteger result =
            bigInteger.compareTo(max) > 0 || bigInteger.compareTo(max.negate()) < 0 ?
                bigInteger.mod(mod) :
                bigInteger;
        return result.compareTo(max) >= 0 ? result.subtract(mod) : result;
    }

    public static @NotNull BigInteger toBigInteger(long high, long low) {
        return BigInteger.valueOf(high).shiftLeft(64).add(UnsignedLong.fromLongBits(low).bigIntegerValue());
    }
}
