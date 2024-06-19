package io.spbx.util.base;

import com.google.common.annotations.Beta;
import com.google.common.base.Strings;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import com.google.errorprone.annotations.Immutable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Represents the signed 128-bit integer.
 */
@Immutable
@Beta
// FIX[perf]: optimize conversions avoiding extra array/BigInteger alloc (arithmetic, toString)
public final class DoubleLong extends Number implements Comparable<DoubleLong> {
    /**
     * The number of bytes required to represent the value.
     */
    public static final int BYTES = 2 * Long.BYTES;
    /**
     * The number of bits used to represent the value in two's complement binary form.
     */
    public static final int BITS = BYTES * Byte.SIZE;

    public static final DoubleLong ZERO = fromBits(0, 0);
    public static final DoubleLong ONE = fromBits(0, 1);
    public static final DoubleLong MAX_VALUE = fromBits(0x7fff_ffff_ffff_ffffL, 0xffff_ffff_ffff_ffffL);
    public static final DoubleLong MIN_VALUE = fromBits(0x8000_0000_0000_0000L, 0x0000_0000_0000_0000L);

    public static final Comparator<DoubleLong> COMPARATOR = DoubleLong::compare;

    private final long high;
    private final long low;

    private DoubleLong(long high, long low) {
        this.high = high;
        this.low = low;
    }

    public static @NotNull DoubleLong fromBits(long highBits, long lowBits) {
        return new DoubleLong(highBits, lowBits);
    }

    public static @NotNull DoubleLong fromBits(long @NotNull [] longs) {
        assert longs.length == 2 : "Invalid long[] length: expected=%d, actual=%d".formatted(2, longs.length);
        return fromBits(longs[0], longs[1]);
    }

    public static @NotNull DoubleLong fromBits(int @NotNull [] ints) {
        assert ints.length == 4 : "Invalid int[] length: expected=%d, actual=%d".formatted(4, ints.length);
        long high = intsToLong(ints[0], ints[1]);
        long low = intsToLong(ints[2], ints[3]);
        return fromBits(high, low);
    }

    public static @NotNull DoubleLong fromBits(byte @NotNull [] bytes) {
        assert bytes.length == BYTES : "Invalid byte[] length: expected=%d, actual=%d".formatted(BYTES, bytes.length);
        long high = Longs.fromBytes(bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5], bytes[6], bytes[7]);
        long low = Longs.fromBytes(bytes[8], bytes[9], bytes[10], bytes[11], bytes[12], bytes[13], bytes[14], bytes[15]);
        return fromBits(high, low);
    }

    public static @NotNull DoubleLong from(@NotNull BigInteger value) {
        byte[] bytes = resizePreservingSign(value.toByteArray());
        return fromBits(bytes);
    }

    public static @NotNull DoubleLong from(@NotNull String value) {
        return from(new BigInteger(value));
    }

    public static @NotNull DoubleLong fromHex(@NotNull String value) {
        return from(new BigInteger(value, 16));
    }

    public static @NotNull DoubleLong from(long value) {
        return value >= 0 ? fromBits(0, value) : fromBits(-1, value);
    }

    public static @NotNull DoubleLong from(double value) {
        return from(toBigInteger(value));
    }

    // Bit-level conversions

    public long highBits() {
        return high;
    }

    public long lowBits() {
        return low;
    }

    public byte @NotNull [] toByteArray() {
        return Bytes.concat(Longs.toByteArray(high), Longs.toByteArray(low));
    }

    // toByteArray(byte[] bytes)
    // toByteArray(byte[] bytes, int from)
    // toUnsignedLong
    // fromUnsignedLong

    public long @NotNull [] toLongArray() {
        return new long[] { high, low };
    }

    public int @NotNull [] toIntArray() {
        return new int[] { toHighInt(high), toLowInt(high), toHighInt(low), toLowInt(low) };
    }

    public @NotNull BigInteger toBigInteger() {
        return new BigInteger(toByteArray());
    }

    // Comparison

    @Override
    public int compareTo(@NotNull DoubleLong other) {
        return compare(this, other);
    }

    public static int compare(@NotNull DoubleLong lhs, @NotNull DoubleLong rhs) {
        int compare = Longs.compare(lhs.high, rhs.high);
        if (compare != 0) return compare;
        return Longs.compare(lhs.low, rhs.low);
    }

    public int signum() {
        return high != 0 ? Long.signum(high) : low == 0 ? 0 : 1;
    }

    // `Number` conversions

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

    public boolean fitsIntoLong() {
        return high == 0;
    }

    // Math and arithmetic

    public static @NotNull DoubleLong add(@NotNull DoubleLong lhs, @NotNull DoubleLong rhs) {
        return from(lhs.toBigInteger().add(rhs.toBigInteger()));
    }

    public static @NotNull DoubleLong subtract(@NotNull DoubleLong lhs, @NotNull DoubleLong rhs) {
        return from(lhs.toBigInteger().subtract(rhs.toBigInteger()));
    }

    public static @NotNull DoubleLong multiply(@NotNull DoubleLong lhs, @NotNull DoubleLong rhs) {
        return from(lhs.toBigInteger().multiply(rhs.toBigInteger()));
    }

    public static @NotNull DoubleLong divide(@NotNull DoubleLong lhs, @NotNull DoubleLong rhs) {
        return from(lhs.toBigInteger().divide(rhs.toBigInteger()));
    }

    public static @NotNull DoubleLong and(@NotNull DoubleLong lhs, @NotNull DoubleLong rhs) {
        return DoubleLong.fromBits(lhs.high & rhs.high, lhs.low & rhs.low);
    }

    public static @NotNull DoubleLong or(@NotNull DoubleLong lhs, @NotNull DoubleLong rhs) {
        return DoubleLong.fromBits(lhs.high | rhs.high, lhs.low | rhs.low);
    }

    public static @NotNull DoubleLong xor(@NotNull DoubleLong lhs, @NotNull DoubleLong rhs) {
        return DoubleLong.fromBits(lhs.high ^ rhs.high, lhs.low ^ rhs.low);
    }

    public static @NotNull DoubleLong max(@NotNull DoubleLong lhs, @NotNull DoubleLong rhs) {
        return compare(lhs, rhs) > 0 ? lhs : rhs;
    }

    public static @NotNull DoubleLong min(@NotNull DoubleLong lhs, @NotNull DoubleLong rhs) {
        return compare(lhs, rhs) < 0 ? lhs : rhs;
    }

    // `Object` methods

    @Override
    public boolean equals(Object object) {
        return object instanceof DoubleLong that && high == that.high && low == that.low;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(high) ^ Long.hashCode(low);
    }

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

    public @NotNull String toHexString() {
        return padded(Long.toHexString(high), 2 * Long.BYTES) + padded(Long.toHexString(low), 2 * Long.BYTES);
    }

    // Implementation details

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
}