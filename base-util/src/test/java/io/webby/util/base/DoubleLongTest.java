package io.webby.util.base;

import com.google.common.base.Strings;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.testing.TestingPrimitives.fitsIntoLong;
import static io.webby.testing.TestingPrimitives.toBigInteger;

public class DoubleLongTest {
    @Test
    public void construction_positive_numbers() {
        assertConstruction("0");
        assertConstruction("1");
        assertConstruction("42");
        assertConstruction("255");
        assertConstruction("256");

        assertConstruction("32767");
        assertConstruction("32768");
        assertConstruction("2147483647");
        assertConstruction("2147483648");
        assertConstruction("9223372036854775807");
        assertConstruction("9223372036854775808");

        assertConstruction("10000000000000000000000000");
        assertConstruction("99999999999999999999999999");
        assertConstruction("99405973409652309870353134");
    }

    @Test
    public void construction_negative_numbers() {
        assertConstruction("-1");
        assertConstruction("-42");
        assertConstruction("-255");
        assertConstruction("-256");

        assertConstruction("-32767");
        assertConstruction("-32768");
        assertConstruction("-2147483647");
        assertConstruction("-2147483648");
        assertConstruction("-9223372036854775807");
        assertConstruction("-9223372036854775808");

        assertConstruction("-10000000000000000000000000");
        assertConstruction("-99999999999999999999999999");
        assertConstruction("-99405973409652309870353134");
    }

    @Test
    public void internal_consistency_trivial() {
        assertThatDoubleLong(DoubleLong.ZERO).internalConsistency();
        assertThatDoubleLong(DoubleLong.ONE).internalConsistency();
        assertThatDoubleLong(DoubleLong.MAX_VALUE).internalConsistency();
        assertThatDoubleLong(DoubleLong.MIN_VALUE).internalConsistency();
    }

    @Test
    public void internal_consistency_long_numbers() {
        assertThatDoubleLong(DoubleLong.from(Byte.MAX_VALUE)).internalConsistency();
        assertThatDoubleLong(DoubleLong.from(Byte.MIN_VALUE)).internalConsistency();
        assertThatDoubleLong(DoubleLong.from(Short.MAX_VALUE)).internalConsistency();
        assertThatDoubleLong(DoubleLong.from(Short.MIN_VALUE)).internalConsistency();
        assertThatDoubleLong(DoubleLong.from(Integer.MAX_VALUE)).internalConsistency();
        assertThatDoubleLong(DoubleLong.from(Integer.MIN_VALUE)).internalConsistency();
        assertThatDoubleLong(DoubleLong.from(Long.MAX_VALUE)).internalConsistency();
        assertThatDoubleLong(DoubleLong.from(Long.MIN_VALUE)).internalConsistency();
    }

    @Test
    public void internal_consistency_big_numbers() {
        assertThatDoubleLong(DoubleLong.from("14262866606244585389")).internalConsistency();
        assertThatDoubleLong(DoubleLong.from("38271921788684024801")).internalConsistency();
        assertThatDoubleLong(DoubleLong.from("511502705152331397313213352309")).internalConsistency();
        assertThatDoubleLong(DoubleLong.from("840915912219908665770642669703")).internalConsistency();

        assertThatDoubleLong(DoubleLong.from("-14262866606244585389")).internalConsistency();
        assertThatDoubleLong(DoubleLong.from("-38271921788684024801")).internalConsistency();
        assertThatDoubleLong(DoubleLong.from("-511502705152331397313213352309")).internalConsistency();
        assertThatDoubleLong(DoubleLong.from("-840915912219908665770642669703")).internalConsistency();
    }

    @Test
    public void trivial_zero() {
        assertThat(DoubleLong.ZERO.highBits()).isEqualTo(0);
        assertThat(DoubleLong.ZERO.lowBits()).isEqualTo(0);
        assertThat(DoubleLong.ZERO.toBigInteger()).isEqualTo(BigInteger.ZERO);
        assertThat(DoubleLong.ZERO.longValue()).isEqualTo(0);
        assertThat(DoubleLong.ZERO.intValue()).isEqualTo(0);
        assertThat(DoubleLong.ZERO.toString()).isEqualTo("0");
        assertThat(DoubleLong.ZERO.toBinaryString()).isEqualTo("0".repeat(128));
    }

    @Test
    public void trivial_one() {
        assertThat(DoubleLong.ONE.highBits()).isEqualTo(0);
        assertThat(DoubleLong.ONE.lowBits()).isEqualTo(1);
        assertThat(DoubleLong.ONE.toBigInteger()).isEqualTo(BigInteger.ONE);
        assertThat(DoubleLong.ONE.longValue()).isEqualTo(1);
        assertThat(DoubleLong.ONE.intValue()).isEqualTo(1);
        assertThat(DoubleLong.ONE.toString()).isEqualTo("1");
        assertThat(DoubleLong.ONE.toBinaryString()).isEqualTo("0".repeat(127) + "1");
    }

    @Test
    public void trivial_max_value() {
        BigInteger expected = BigInteger.ONE.shiftLeft(127).subtract(BigInteger.ONE);  // (1 << 127) - 1
        assertThat(DoubleLong.MAX_VALUE.highBits()).isEqualTo(Long.MAX_VALUE);
        assertThat(DoubleLong.MAX_VALUE.lowBits()).isEqualTo(-1);
        assertThat(DoubleLong.MAX_VALUE.toBigInteger()).isEqualTo(expected);
        assertThat(DoubleLong.MAX_VALUE.longValue()).isEqualTo(expected.longValue());  // -1
        assertThat(DoubleLong.MAX_VALUE.intValue()).isEqualTo(expected.intValue());    // -1
        assertThat(DoubleLong.MAX_VALUE.toString()).isEqualTo(expected.toString());
        assertThat(DoubleLong.MAX_VALUE.toBinaryString()).isEqualTo("0" + "1".repeat(127));
    }

    @Test
    public void trivial_min_value() {
        BigInteger expected = BigInteger.ONE.shiftLeft(127).negate();  // -(1 << 127)
        assertThat(DoubleLong.MIN_VALUE.highBits()).isEqualTo(Long.MIN_VALUE);
        assertThat(DoubleLong.MIN_VALUE.lowBits()).isEqualTo(0);
        assertThat(DoubleLong.MIN_VALUE.toBigInteger()).isEqualTo(expected);
        assertThat(DoubleLong.MIN_VALUE.longValue()).isEqualTo(expected.longValue());  // 0
        assertThat(DoubleLong.MIN_VALUE.intValue()).isEqualTo(expected.intValue());    // 0
        assertThat(DoubleLong.MIN_VALUE.toString()).isEqualTo(expected.toString());
        assertThat(DoubleLong.MIN_VALUE.toBinaryString()).isEqualTo("1" + "0".repeat(127));
    }

    @CheckReturnValue
    private @NotNull DoubleLongSubject assertThatDoubleLong(@NotNull DoubleLong actual) {
        return new DoubleLongSubject(actual);
    }

    @CanIgnoreReturnValue
    private record DoubleLongSubject(@NotNull DoubleLong actual) {
        public @NotNull DoubleLongSubject isEqualTo(@NotNull DoubleLong other) {
            assertEquality(actual, other);
            return this;
        }

        public @NotNull DoubleLongSubject internalConsistency() {
            return isEqualTo(actual)
                .roundtripLongBits()
                .roundtripByteArrayBits()
                .roundtripIntArrayBits()
                .roundtripLongArrayBits()
                .roundtripString()
                .roundtripBigInteger()
                .roundtripLong()
                .internalConstruction();
        }

        public @NotNull DoubleLongSubject roundtripLongBits() {
            DoubleLong copy = DoubleLong.fromBits(actual.highBits(), actual.lowBits());
            return isEqualTo(copy);
        }

        public @NotNull DoubleLongSubject roundtripByteArrayBits() {
            byte[] bytes = actual.toByteArray();
            DoubleLong copy = DoubleLong.fromBits(bytes);
            return isEqualTo(copy);
        }

        public @NotNull DoubleLongSubject roundtripIntArrayBits() {
            int[] ints = actual.toIntArray();
            DoubleLong copy = DoubleLong.fromBits(ints);
            return isEqualTo(copy);
        }

        public @NotNull DoubleLongSubject roundtripLongArrayBits() {
            long[] longs = actual.toLongArray();
            DoubleLong copy = DoubleLong.fromBits(longs);
            return isEqualTo(copy);
        }

        public @NotNull DoubleLongSubject roundtripString() {
            String str = actual.toString();
            DoubleLong copy = DoubleLong.from(str);
            return isEqualTo(copy);
        }

        public @NotNull DoubleLongSubject roundtripBigInteger() {
            BigInteger bigInteger = actual.toBigInteger();
            DoubleLong copy = DoubleLong.from(bigInteger);
            return isEqualTo(copy);
        }

        public @NotNull DoubleLongSubject roundtripLong() {
            String str = actual.toString();
            try {
                long parsed = Long.parseLong(str);
                DoubleLong copy = DoubleLong.from(parsed);
                return isEqualTo(copy);
            } catch (NumberFormatException e) {
                // Not applicable
                return this;
            }
        }

        public @NotNull DoubleLongSubject internalConstruction() {
            assertConstruction(actual.toString());
            return this;
        }
    }

    private static void assertEquality(@NotNull DoubleLong lhs, @NotNull DoubleLong rhs) {
        assertThat(lhs.highBits()).isEqualTo(rhs.highBits());
        assertThat(lhs.lowBits()).isEqualTo(rhs.lowBits());

        assertThat(lhs.equals(rhs)).isTrue();
        assertThat(rhs.equals(lhs)).isTrue();
        assertThat(lhs.hashCode()).isEqualTo(rhs.hashCode());
        assertThat(lhs.toString()).isEqualTo(rhs.toString());

        assertThat(lhs.compareTo(rhs)).isEqualTo(0);
        assertThat(rhs.compareTo(lhs)).isEqualTo(0);
        assertThat(DoubleLong.compare(lhs, rhs)).isEqualTo(0);
        assertThat(DoubleLong.compare(rhs, lhs)).isEqualTo(0);
        assertThat(DoubleLong.COMPARATOR.compare(lhs, rhs)).isEqualTo(0);
        assertThat(DoubleLong.COMPARATOR.compare(rhs, lhs)).isEqualTo(0);
    }

    private static void assertConstruction(@NotNull String str) {
        BigInteger bigInteger = new BigInteger(str);

        assertConstructionFromString(str);
        assertConstructionFromBigInteger(bigInteger);
        assertConstructionFromHexString(bigInteger.toString(16));

        if (fitsIntoLong(bigInteger)) {
            long num = Long.parseLong(str);
            assertConstructionFromLong(num);
            assertConstructionFromLongBits(0, num);
            assertConstructionFromLongArrayBits(new long[] { 0, num });
        } else {
            long high = bigInteger.shiftRight(64).longValue();
            long low = bigInteger.longValue();
            assertConstructionFromLongBits(high, low);
            assertConstructionFromLongArrayBits(new long[] { high, low });
        }
    }

    private static void assertConstructionFromString(@NotNull String str) {
        DoubleLong value = DoubleLong.from(str);
        assertThat(value.toString()).isEqualTo(str);
        assertThat(value.toBigInteger()).isEqualTo(new BigInteger(str));
    }

    private static void assertConstructionFromHexString(@NotNull String hex) {
        DoubleLong value = DoubleLong.fromHex(hex);
        assertThat(value.toBigInteger()).isEqualTo(new BigInteger(hex, 16));
        // hex string?
    }

    private static void assertConstructionFromBigInteger(@NotNull BigInteger bigInteger) {
        DoubleLong value = DoubleLong.from(bigInteger);

        assertThat(value.toBigInteger()).isEqualTo(bigInteger);
        assertThat(value.signum()).isEqualTo(bigInteger.signum());

        assertThat(value.longValue()).isEqualTo(bigInteger.longValue());
        assertThat(value.intValue()).isEqualTo(bigInteger.intValue());
        assertThat(value.shortValue()).isEqualTo(bigInteger.shortValue());
        assertThat(value.byteValue()).isEqualTo(bigInteger.byteValue());
        assertThat(value.floatValue()).isEqualTo(bigInteger.floatValue());
        assertThat(value.doubleValue()).isEqualTo(bigInteger.doubleValue());

        assertThat(value.toString()).isEqualTo(bigInteger.toString());
        assertThat(value.toString(2)).isEqualTo(bigInteger.toString(2));
        assertThat(value.toString(8)).isEqualTo(bigInteger.toString(8));
        assertThat(value.toString(16)).isEqualTo(bigInteger.toString(16));

        if (bigInteger.compareTo(BigInteger.ZERO) >= 0) {
            assertThat(value.toBinaryString()).isEqualTo(Strings.padStart(bigInteger.toString(2), 128, '0'));
            assertThat(value.toHexString()).isEqualTo(Strings.padStart(bigInteger.toString(16), 32, '0'));
        }
    }

    private static void assertConstructionFromLong(long num) {
        DoubleLong value = DoubleLong.from(num);

        assertThat(value.longValue()).isEqualTo(num);
        assertThat(value.intValue()).isEqualTo((int) num);
        assertThat(value.shortValue()).isEqualTo((short) num);
        assertThat(value.byteValue()).isEqualTo((byte) num);
        assertThat(value.floatValue()).isEqualTo((float) num);
        assertThat(value.doubleValue()).isEqualTo((double) num);

        assertThat(value.toString()).isEqualTo(String.valueOf(num));
        assertThat(value.toBigInteger()).isEqualTo(new BigInteger(String.valueOf(num)));

        if (num >= 0) {
            assertThat(value.highBits()).isEqualTo(0);
            assertThat(value.lowBits()).isEqualTo(num);
            assertThat(value.toBinaryString()).isEqualTo(Strings.padStart(Long.toBinaryString(num), 128, '0'));
        } else {
            assertThat(value.highBits()).isEqualTo(-1);
            assertThat(value.lowBits()).isEqualTo(num);
            assertThat(value.toBinaryString()).isEqualTo(Strings.padStart(Long.toBinaryString(num), 128, '1'));
        }
    }

    private static void assertConstructionFromDouble(double num) {
        DoubleLong value = DoubleLong.from(num);

        assertThat(value.longValue()).isEqualTo((long) num);
        assertThat(value.intValue()).isEqualTo((int) num);
        assertThat(value.shortValue()).isEqualTo((short) num);
        assertThat(value.byteValue()).isEqualTo((byte) num);
        assertThat(value.floatValue()).isEqualTo((float) num);
        assertThat(value.doubleValue()).isEqualTo(num);

        assertThat(value.toString()).isEqualTo(String.valueOf((long) num));
        assertThat(value.toBigInteger()).isEqualTo(BigInteger.valueOf((long) num));
    }

    private static void assertConstructionFromLongBits(long high, long low) {
        DoubleLong value = DoubleLong.fromBits(high, low);
        assertThat(value.highBits()).isEqualTo(high);
        assertThat(value.lowBits()).isEqualTo(low);

        assertThat(value.longValue()).isEqualTo(low);
        assertThat(value.intValue()).isEqualTo((int) low);
        assertThat(value.toBigInteger()).isEqualTo(toBigInteger(high, low));
    }

    private static void assertConstructionFromLongArrayBits(long[] longs) {
        DoubleLong value = DoubleLong.fromBits(longs);
        assertThat(value.highBits()).isEqualTo(longs[0]);
        assertThat(value.lowBits()).isEqualTo(longs[1]);

        assertThat(value.longValue()).isEqualTo(longs[1]);
        assertThat(value.intValue()).isEqualTo((int) longs[1]);
        assertThat(value.toBigInteger()).isEqualTo(toBigInteger(longs[0], longs[1]));
    }
}
