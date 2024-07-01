package io.spbx.util.base;

import com.google.common.annotations.Beta;
import com.google.common.base.Strings;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import com.google.common.primitives.UnsignedLong;
import com.google.errorprone.annotations.Immutable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Comparator;

import static io.spbx.util.base.EasyExceptions.*;

/**
 * Represents the signed 128-bit integer or double {@link Long} value.
 * Preserves the semantics of {@link BigInteger} (e.g., two's compliment binary representation).
 * Is much more efficient both in terms of memory (stores two {@code long}s) and operations speed.
 *
 * @see Long
 * @see BigInteger
 * @link <a href="https://en.wikipedia.org/wiki/Two%27s_complement">Two's complement</a>
 */
@Immutable
@Beta // FIX[perf]: optimize conversions avoiding extra array/BigInteger alloc (arithmetic, toString)
public final class Int128 extends Number implements Comparable<Int128> {
    /**
     * The number of bytes required to represent the value.
     */
    public static final int BYTES = 2 * Long.BYTES;
    /**
     * The number of bits used to represent the value in two's complement binary form.
     */
    public static final int BITS = BYTES * Byte.SIZE;

    public static final Int128 ZERO = fromBits(0, 0);
    public static final Int128 ONE = fromBits(0, 1);
    public static final Int128 MAX_VALUE = fromBits(0x7fff_ffff_ffff_ffffL, 0xffff_ffff_ffff_ffffL);
    public static final Int128 MIN_VALUE = fromBits(0x8000_0000_0000_0000L, 0x0000_0000_0000_0000L);

    public static final Comparator<Int128> COMPARATOR = Int128::compare;
    public static final Comparator<Int128> COMPARATOR_UNSIGNED = Int128::compareUnsigned;

    private final long high;
    private final long low;

    private Int128(long high, long low) {
        this.high = high;
        this.low = low;
    }

    /* Construction */

    public static @NotNull Int128 fromBits(long highBits, long lowBits) {
        return new Int128(highBits, lowBits);
    }

    public static @NotNull Int128 fromBits(long @NotNull [] longs) {
        assert longs.length == 2 : "Invalid long[] length: expected=%d, actual=%d".formatted(2, longs.length);
        return fromBits(longs[0], longs[1]);
    }

    public static @NotNull Int128 fromBits(int @NotNull [] ints) {
        assert ints.length == 4 : "Invalid int[] length: expected=%d, actual=%d".formatted(4, ints.length);
        long high = intsToLong(ints[0], ints[1]);
        long low = intsToLong(ints[2], ints[3]);
        return fromBits(high, low);
    }

    public static @NotNull Int128 fromBits(byte @NotNull [] bytes) {
        assert bytes.length == BYTES : "Invalid byte[] length: expected=%d, actual=%d".formatted(BYTES, bytes.length);
        long high = Longs.fromBytes(bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5], bytes[6], bytes[7]);
        long low = Longs.fromBytes(bytes[8], bytes[9], bytes[10], bytes[11], bytes[12], bytes[13], bytes[14], bytes[15]);
        return fromBits(high, low);
    }

    public static @NotNull Int128 from(@NotNull BigInteger value) {
        byte[] bytes = resizePreservingSign(value.toByteArray());
        return fromBits(bytes);
    }

    public static @NotNull Int128 from(@NotNull UnsignedLong value) {
        return from(value.longValue());
    }

    public static @NotNull Int128 from(@NotNull CharSequence value) {
        return from(new BigInteger(value.toString()));
    }

    public static @NotNull Int128 fromHex(@NotNull String value) {
        boolean minus = value.startsWith("-");
        int start = value.startsWith("-0x") ? 3 : value.startsWith("0x") ? 2 : value.startsWith("-") ? 1 : 0;
        long high = 0, low = 0;
        for (int i = value.length() - 1, bit = 0; i >= start; i--, bit++) {
            char ch = value.charAt(i);
            if (ch == '_') {
                bit--;
                continue;
            }
            long bitVal = parseHexChar(ch);
            if (bit >= 16) {
                high += bitVal << (bit - 16) * 4;
            } else {
                low += bitVal << bit * 4;
            }
        }
        return minus ? fromBitsFlipSign(high, low) : fromBits(high, low);
    }

    public static @NotNull Int128 from(long value) {
        // Long version:
        //   return value >= 0 ? fromBits(0, value) : fromBits(-1, value);
        return fromBits(fastZeroOrMinusOne(value), value);
    }

    @Beta
    public static @NotNull Int128 from(double value) {
        return from(toBigInteger(value));
    }

    /* Bit-level conversions */

    public long highBits() {
        return high;
    }

    public long lowBits() {
        return low;
    }

    // Optimize and add more utils:
    // toByteArray(byte[] bytes)
    // toByteArray(byte[] bytes, int from)
    public byte @NotNull [] toByteArray() {
        return Bytes.concat(Longs.toByteArray(high), Longs.toByteArray(low));
    }

    public long @NotNull [] toLongArray() {
        return new long[] { high, low };
    }

    public int @NotNull [] toIntArray() {
        return new int[] { toHighInt(high), toLowInt(high), toHighInt(low), toLowInt(low) };
    }

    public @NotNull BigInteger toBigInteger() {
        return new BigInteger(toByteArray());
    }

    public @NotNull UnsignedLong toUnsignedLong() {
        assert is64Bit() : "The value does not fit into `UnsignedLong`";
        return UnsignedLong.fromLongBits(low);
    }

    /* Comparison */

    @Override
    public int compareTo(@NotNull Int128 that) {
        return compare(this.high, this.low, that.high, that.low);
    }

    public static int compare(@NotNull Int128 lhs, @NotNull Int128 rhs) {
        return compare(lhs.high, lhs.low, rhs.high, rhs.low);
    }

    private static int compare(long hi1, long lo1, long hi2, long lo2) {
        int cmp = Longs.compare(hi1, hi2);
        return cmp != 0 ? cmp : Long.compareUnsigned(lo1, lo2);
    }

    public static int compareUnsigned(@NotNull Int128 lhs, @NotNull Int128 rhs) {
        return compareUnsigned(lhs.high, lhs.low, rhs.high, rhs.low);
    }

    private static int compareUnsigned(long hi1, long lo1, long hi2, long lo2) {
        int cmp = Long.compareUnsigned(hi1, hi2);
        return cmp != 0 ? cmp : Long.compareUnsigned(lo1, lo2);
    }

    public static @NotNull Int128 max(@NotNull Int128 lhs, @NotNull Int128 rhs) {
        return compare(lhs, rhs) > 0 ? lhs : rhs;
    }

    public static @NotNull Int128 min(@NotNull Int128 lhs, @NotNull Int128 rhs) {
        return compare(lhs, rhs) < 0 ? lhs : rhs;
    }

    /* Sign */

    public int signum() {
        return high != 0 ? Long.signum(high) : low == 0 ? 0 : 1;
    }

    // Note: `DoubleLong.MIN_VALUE.negate() == DoubleLong.MIN_VALUE`!
    // https://stackoverflow.com/questions/5444611/math-abs-returns-wrong-value-for-integer-min-value
    public @NotNull Int128 negate() {
        return fromBitsFlipSign(high, low);
    }

    private static @NotNull Int128 fromBitsFlipSign(long high, long low) {
        // General formula: -X = ~X + 1. The "+1" can be further optimized because usually it only changes the `low`
        return low == 0 ? Int128.fromBits(~high + 1, 0) : Int128.fromBits(~high, ~low + 1);
    }

    // Note: `DoubleLong.MIN_VALUE.abs() == DoubleLong.MIN_VALUE`!
    // https://stackoverflow.com/questions/5444611/math-abs-returns-wrong-value-for-integer-min-value
    public @NotNull Int128 abs() {
        return high >= 0 ? this : this.negate();
    }

    private @NotNull Int128 flipSign(boolean flip) {
        return flip ? this.negate() : this;
    }

    /* `Number` conversions */

    @Override
    public int intValue() {
        return (int) low;
    }

    @Override
    public long longValue() {
        return low;
    }

    @Override
    public float floatValue() {
        return toBigInteger().floatValue();
    }

    @Override
    public double doubleValue() {
        return toBigInteger().doubleValue();
    }

    public boolean is64Bit() {
        return (high == 0 && low >= 0) || (high == -1 && low < 0);
    }

    /* Math and arithmetic */

    public @NotNull Int128 increment() {
        return low == -1 ? Int128.fromBits(high + 1, 0) : Int128.fromBits(high, low + 1);
    }

    public @NotNull Int128 decrement() {
        return low == 0 ? Int128.fromBits(high - 1, -1) : Int128.fromBits(high, low - 1);
    }

    public @NotNull Int128 add(@NotNull Int128 that) {
        return add(this.high, this.low, that.high, that.low);
    }

    public @NotNull Int128 add(long value) {
        // Long version:
        //   return value >= 0 ? add(this.high, this.low, 0, value) : add(this.high, this.low, -1, value);
        return add(this.high, this.low, fastZeroOrMinusOne(value), value);
    }

    private static @NotNull Int128 add(long hi1, long lo1, long hi2, long lo2) {
        // Notes:
        // - zero comparison calculates the sign bit: sign_bit(x) = "0" for x >= 0 and "1" for x < 0
        // - should carry if the sum rolls over 2^64, not 2^63 (which is Long overflow)
        // - if the sign_bit gets lots in a result (either `lo1` or `lo2`), there is a carry
        // - if both operands had the sign bit, there is a carry
        long lo_sum = lo1 + lo2;
        boolean carry = ((lo1 & lo2) < 0) || ((lo1 ^ lo2) < 0 & lo_sum >= 0);
        long hi_sum = hi1 + hi2 + (carry ? 1L : 0L);
        return fromBits(hi_sum, lo_sum);
    }

    public @NotNull Int128 subtract(@NotNull Int128 that) {
        return subtract(this.high, this.low, that.high, that.low);
    }

    private static @NotNull Int128 subtract(long hi1, long lo1, long hi2, long lo2) {
        // Long version:
        //   add(this, that.negate());
        return lo2 == 0 ? add(hi1, lo1, ~hi2 + 1, 0) : add(hi1, lo1, ~hi2, ~lo2 + 1);
    }

    public @NotNull Int128 subtract(long value) {
        return value == Long.MIN_VALUE ? subtract(from(value)) : add(-value);
    }

    public @NotNull Int128 multiply(@NotNull Int128 that) {
        return multiply(this.high, this.low, that.high, that.low);
    }

    public @NotNull Int128 multiply(long value) {
        // Long version:
        //   return value >= 0 ? multiply(this.high, this.low, 0, value) : multiply(this.high, this.low, -1, value);
        return multiply(this.high, this.low, fastZeroOrMinusOne(value), value);
    }

    private static @NotNull Int128 multiply(long hi1, long lo1, long hi2, long lo2) {
        // https://stackoverflow.com/questions/18859207/high-bits-of-long-multiplication-in-java
        long low = lo1 * lo2;
        long high = Math.unsignedMultiplyHigh(lo1, lo2) + hi1 * lo2 + hi2 * lo1;
        return fromBits(high, low);
    }

    public @NotNull Int128 divide(@NotNull Int128 that) {
        assert that.high != 0 || that.low != 0 : "Division by zero: %s / 0".formatted(this);

        Int128 a = this.abs();
        Int128 b = that.abs();
        boolean sameSign = this.high < 0 == that.high < 0;
        if (a.high == Long.MIN_VALUE) {
          return a.subtract(b).divide(b).increment().flipSign(!sameSign);
        } if (b.high == 0) {
            return divide64BitUnsigned(a.high, a.low, b.low).flipSign(!sameSign);
        } else {
            return divide128BitUnsigned(a.high, a.low, b.high, b.low).flipSign(!sameSign);
        }
    }

    private static @NotNull Int128 divide64BitUnsigned(long hi, long lo, long num) {
        if (num == 1) {
            return Int128.fromBits(hi, lo);
        }
        if (hi == 0) {
            return Int128.from(Long.divideUnsigned(lo, num));
        }

        int n = Long.numberOfLeadingZeros(num) - Long.numberOfLeadingZeros(hi) + 64;
        while (n >= 0) {
            Int128 div = Int128.ONE.shiftLeft(n);
            Int128 multiply = Int128.fromBits(0, num).shiftLeft(n);
            if (compareUnsigned(hi, lo, multiply.high, multiply.low) >= 0) {
                Int128 sub = subtract(hi, lo, multiply.high, multiply.low);
                return divide64BitUnsigned(sub.high, sub.low, num).add(div);
            }
            n--;
        }

        throw newInternalError("Failed to divide %s / %s", Int128.fromBits(hi, lo), num);
    }

    private static @NotNull Int128 divide128BitUnsigned(long hi1, long lo1, long hi2, long lo2) {
        InternalErrors.failIf(hi2 == 0, "The method must not be called, call divide64BitUnsigned()");

        if (Long.compareUnsigned(hi1, hi2) < 0) {
            return Int128.ZERO;
        }
        if (Long.compareUnsigned(hi1, hi2) == 0) {
            return Long.compareUnsigned(lo1, lo2) < 0 ? Int128.ZERO : Int128.ONE;
        }

        long div = Long.divideUnsigned(hi1, hi2);
        while (div > 0) {
            Int128 multiply = div == 1 ? Int128.fromBits(hi2, lo2) : Int128.multiply(0, div, hi2, lo2);
            if (compareUnsigned(hi1, lo1, multiply.high, multiply.low) >= 0) {
                Int128 sub = subtract(hi1, lo1, multiply.high, multiply.low);
                return divide128BitUnsigned(sub.high, sub.low, hi2, lo2).add(div);
            }
            div = div >> 1;
        }

        throw newInternalError("Failed to divide %s / %s", fromBits(hi1, lo1), fromBits(hi2, lo2));
    }

    /* Logical ops */

    public @NotNull Int128 not() {
        return Int128.fromBits(~high, ~low);
    }

    public @NotNull Int128 and(@NotNull Int128 that) {
        return Int128.fromBits(this.high & that.high, this.low & that.low);
    }

    public @NotNull Int128 andNot(@NotNull Int128 that) {
        return Int128.fromBits(this.high & ~that.high, this.low & ~that.low);
    }

    public @NotNull Int128 or(@NotNull Int128 that) {
        return Int128.fromBits(this.high | that.high, this.low | that.low);
    }

    public @NotNull Int128 xor(@NotNull Int128 that) {
        return Int128.fromBits(this.high ^ that.high, this.low ^ that.low);
    }

    public @NotNull Int128 shiftLeft(int len) {
        if (len < 0) return shiftRight(-len);
        if (len == 0) return this;
        return len < 64 ?
            fromBits((high << len) + (low >>> (64 - len)), low << len) :
            fromBits(low << (len - 64), 0);
    }

    // signed version
    public @NotNull Int128 shiftRight(int len) {
        if (len < 0) return shiftLeft(-len);
        if (len == 0) return this;
        return len < 64 ?
            fromBits(high >> len, ((high & ((1L << len) - 1)) << (64 - len)) + (low >>> len)) :
            high < 0 ?
                fromBits(-1, high >> (len - 64)) :
                fromBits(0, high >>> (len - 64));
    }

    // unsigned version
    public @NotNull Int128 shiftRightUnsigned(int len) {
        if (len < 0) return shiftLeft(-len);
        if (len == 0) return this;
        return len < 64 ?
            fromBits(high >> len, ((high & ((1L << len) - 1)) << (64 - len)) + (low >>> len)) :
            fromBits(0, high >>> (len - 64));
    }

    /* Bits */

    public int bitAt(int pos) {
        assert pos >= 0 && pos < BITS : "Incorrect bit address: " + pos;
        return (int) (pos < 64 ? (low >> pos) & 1 : (high >> (pos - 64)) & 1);
    }

    public @NotNull Int128 setBitAt(int pos) {
        assert pos >= 0 && pos < BITS : "Incorrect bit address: " + pos;
        return pos < 64 ? new Int128(high, low | (1L << pos)) : new Int128(high | (1L << (pos - 64)), low);
    }

    public @NotNull Int128 clearBitAt(int pos) {
        assert pos >= 0 && pos < BITS : "Incorrect bit address: " + pos;
        return new Int128(pos < 64 ? high : high & ~(1L << (pos - 64)), pos < 64 ? low & ~(1L << pos) : low);
    }

    public @NotNull Int128 flipBitAt(int pos) {
        assert pos >= 0 && pos < BITS : "Incorrect bit address: " + pos;
        return new Int128(pos < 64 ? high : high ^ (1L << (pos - 64)), pos < 64 ? low ^ (1L << pos) : low);
    }

    public int numberOfLeadingZeros() {
        return high == 0 ? Long.numberOfLeadingZeros(low) + 64 : Long.numberOfLeadingZeros(high);
    }

    public int numberOfTrailingZeros() {
        return low == 0 ? Long.numberOfTrailingZeros(high) + 64 : Long.numberOfTrailingZeros(low);
    }

    public int bitCount() {
        return Long.bitCount(low) + Long.bitCount(high);
    }

    /* `Object` methods */

    @Override
    public boolean equals(Object object) {
        return object instanceof Int128 that && high == that.high && low == that.low;
    }

    public boolean equals(long value) {
        // Long version:
        //   return low == value && high == (value >= 0 ? 0 : -1);
        return low == value && high == fastZeroOrMinusOne(value);
    }

    @Override
    public int hashCode() {
        return Long.hashCode(high) ^ Long.hashCode(low);
    }

    /* `String` representations */

    @Override
    public @NotNull String toString() {
        return toBigInteger().toString();
    }

    public @NotNull String toString(int radix) {
        return toBigInteger().toString(radix);
    }

    public @NotNull String toBinaryString() {
        return padded(Long.toBinaryString(high), Long.SIZE) + padded(Long.toBinaryString(low), Long.SIZE);
    }

    public @NotNull String toBinaryStringReadable() {
        return makeGroups(toBinaryString(), 32);
    }

    public @NotNull String toHexString() {
        return padded(Long.toHexString(high), 2 * Long.BYTES) + padded(Long.toHexString(low), 2 * Long.BYTES);
    }

    public @NotNull String toHexStringReadable() {
        return makeGroups(toHexString(), 4);
    }

    /* Implementation details */

    /**
     * Branch-less form of <code>return test < 0 ? value : 0;</code> or
     * <pre>
     * if (test < 0) {
     *   return value;
     * }
     * else {
     *   return 0;
     * }
     * </pre>
     */
    @VisibleForTesting
    static long fastZeroOrValue(long test, long value) {
        return value & (test >> 63);
    }

    /**
     * Branch-less form of <code>return test < 0 ? -1 : 0;</code> or <code>return test >= 0 ? 0 : -1;</code>
     */
    @VisibleForTesting
    static long fastZeroOrMinusOne(long test) {
        return test >> 63;
    }

    @VisibleForTesting
    static long intsToLong(int high, int low) {
        return ((long) high << 32) | (low & 0xffffffffL);
    }

    @VisibleForTesting
    static int toHighInt(long value) {
        return (int) (value >> 32);
    }

    @VisibleForTesting
    static int toLowInt(long value) {
        return (int) value;
    }

    private static long parseHexChar(char ch) {
        if (ch >= '0' && ch <= '9') return (long) ch - '0';
        if (ch >= 'a' && ch <= 'f') return (long) ch - 'a' + 10;
        if (ch >= 'A' && ch <= 'F') return (long) ch - 'A' + 10;
        throw newIllegalArgumentException("Invalid hex character found: %s", ch);
    }

    // https://stackoverflow.com/questions/8240704/resizing-a-byte-twos-complement-represented-integer
    @VisibleForTesting
    static byte @NotNull [] resizePreservingSign(byte @NotNull [] bytes) {
        if (bytes.length < BYTES) {
            byte signum = bytes[0] < 0 ? (byte) 0xff : 0;
            byte[] copy = new byte[BYTES];
            Arrays.fill(copy, signum);
            System.arraycopy(bytes, 0, copy, BYTES - bytes.length, bytes.length);
            return copy;
        }
        return bytes;
    }

    // https://stackoverflow.com/questions/17960186/is-there-anyway-to-convert-from-double-to-biginteger
    @VisibleForTesting
    static @NotNull BigInteger toBigInteger(double value) {
        long bits = Double.doubleToLongBits(value);
        int exp = ((int)(bits >> 52) & 0x7ff) - 1075;
        BigInteger result = BigInteger.valueOf((bits & ((1L << 52)) - 1) | (1L << 52)).shiftLeft(exp);
        return bits >= 0 ? result : result.negate();
    }

    private static @NotNull String padded(@NotNull String str, int size) {
        return Strings.padStart(str, size, '0');
    }

    private static @NotNull String makeGroups(@NotNull String str, int groupSize) {
        int length = str.length();
        StringBuilder builder = new StringBuilder(2 * length);
        for (int i = 0; i < length; i += groupSize) {
            builder.append(str, i, i + groupSize).append('_');
        }
        builder.setLength(builder.length() - 1);
        return builder.toString();
    }
}
