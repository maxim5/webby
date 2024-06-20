package io.spbx.util.base;

import com.google.common.base.Strings;
import com.google.common.primitives.UnsignedLong;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.spbx.util.testing.TestingBasics;
import io.spbx.util.testing.TestingPrimitives;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static io.spbx.util.testing.TestingPrimitives.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("EqualsWithItself")
public class DoubleLongTest {
    private static final List<Long> SIMPLE_MIN_MAX_VALUES = Stream.of(
        Byte.MAX_VALUE, Byte.MIN_VALUE,
        Short.MAX_VALUE, Short.MIN_VALUE,
        Integer.MAX_VALUE, Integer.MIN_VALUE,
        Long.MAX_VALUE, Long.MIN_VALUE
    ).map(Number::longValue).toList();

    private static final List<BigInteger> EDGE_CASE_BIG_INTEGERS = IntStream.range(0, 128)
        .mapToObj(BigInteger.ONE::shiftLeft)
        .flatMap(b -> Stream.of(b, b.subtract(BigInteger.ONE), b.negate(), b.negate().add(BigInteger.ONE)))
        .map(TestingPrimitives::fitInto128Bits)
        .sorted().distinct().toList();

    private static final List<Long> EDGE_CASE_LONGS =
        EDGE_CASE_BIG_INTEGERS.stream().map(BigInteger::longValue).sorted().distinct().toList();

    // From https://bigprimes.org/
    private static final List<BigInteger> LARGE_PRIME_NUMBERS = Stream.of(
        "7734245186249221",
        "14262866606244585389",
        "643060765953885798980471",
        "511502705152331397313213352309",
        "729607297303010430697493914704054547"
    ).map(BigInteger::new).flatMap(b -> Stream.of(b, b.negate())).sorted().toList();

    @Test
    public void trivial_zero() {
        assertThat(DoubleLong.ZERO.highBits()).isEqualTo(0);
        assertThat(DoubleLong.ZERO.lowBits()).isEqualTo(0);
        assertThat(DoubleLong.ZERO.toBigInteger()).isEqualTo(BigInteger.ZERO);
        assertThat(DoubleLong.ZERO.longValue()).isEqualTo(0);
        assertThat(DoubleLong.ZERO.intValue()).isEqualTo(0);
        assertThat(DoubleLong.ZERO.toString()).isEqualTo("0");
        assertThat(DoubleLong.ZERO.toBinaryString()).isEqualTo("0".repeat(128));
        assertThat(DoubleLong.ZERO).isEqualTo(DoubleLong.from(0));
        assertThat(DoubleLong.ZERO.compareTo(DoubleLong.ZERO)).isEqualTo(0);
        assertThat(DoubleLong.ZERO.compareTo(DoubleLong.ONE)).isEqualTo(-1);
        assertThat(DoubleLong.ZERO.compareTo(DoubleLong.MIN_VALUE)).isEqualTo(1);
        assertThat(DoubleLong.ZERO.compareTo(DoubleLong.MAX_VALUE)).isEqualTo(-1);
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
        assertThat(DoubleLong.ONE).isEqualTo(DoubleLong.from(1));
        assertThat(DoubleLong.ONE.compareTo(DoubleLong.ZERO)).isEqualTo(1);
        assertThat(DoubleLong.ONE.compareTo(DoubleLong.ONE)).isEqualTo(0);
        assertThat(DoubleLong.ONE.compareTo(DoubleLong.MIN_VALUE)).isEqualTo(1);
        assertThat(DoubleLong.ONE.compareTo(DoubleLong.MAX_VALUE)).isEqualTo(-1);
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
        assertThat(DoubleLong.MAX_VALUE.compareTo(DoubleLong.ZERO)).isEqualTo(1);
        assertThat(DoubleLong.MAX_VALUE.compareTo(DoubleLong.ONE)).isEqualTo(1);
        assertThat(DoubleLong.MAX_VALUE.compareTo(DoubleLong.MIN_VALUE)).isEqualTo(1);
        assertThat(DoubleLong.MAX_VALUE.compareTo(DoubleLong.MAX_VALUE)).isEqualTo(0);
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
        assertThat(DoubleLong.MIN_VALUE.compareTo(DoubleLong.ZERO)).isEqualTo(-1);
        assertThat(DoubleLong.MIN_VALUE.compareTo(DoubleLong.ONE)).isEqualTo(-1);
        assertThat(DoubleLong.MIN_VALUE.compareTo(DoubleLong.MIN_VALUE)).isEqualTo(0);
        assertThat(DoubleLong.MIN_VALUE.compareTo(DoubleLong.MAX_VALUE)).isEqualTo(-1);
    }

    @Test
    public void trivial_construction_from_bits_long() {
        assertThat(DoubleLong.fromBits(0, 0)).isEqualTo(DoubleLong.from(BigInteger.ZERO));
        assertThat(DoubleLong.fromBits(0, 1)).isEqualTo(DoubleLong.from(BigInteger.ONE));
        assertThat(DoubleLong.fromBits(0, -1)).isEqualTo(DoubleLong.from(BigInteger.ONE.shiftLeft(64).subtract(BigInteger.ONE)));

        assertThat(DoubleLong.fromBits(1, 0)).isEqualTo(DoubleLong.from(BigInteger.ONE.shiftLeft(64)));
        assertThat(DoubleLong.fromBits(1, 1)).isEqualTo(DoubleLong.from(BigInteger.ONE.shiftLeft(64).add(BigInteger.ONE)));
        assertThat(DoubleLong.fromBits(1, -1)).isEqualTo(DoubleLong.from(BigInteger.ONE.shiftLeft(65).subtract(BigInteger.ONE)));

        assertThat(DoubleLong.fromBits(-1, 0)).isEqualTo(DoubleLong.from(BigInteger.ONE.shiftLeft(64).negate()));
        assertThat(DoubleLong.fromBits(-1, 1)).isEqualTo(DoubleLong.from(BigInteger.ONE.shiftLeft(64).negate().add(BigInteger.ONE)));
        assertThat(DoubleLong.fromBits(-1, -1)).isEqualTo(DoubleLong.from(BigInteger.ONE.negate()));

        assertThat(DoubleLong.fromBits(0, Long.MAX_VALUE)).isEqualTo(DoubleLong.from(BigInteger.valueOf(Long.MAX_VALUE)));
        assertThat(DoubleLong.fromBits(0, Long.MIN_VALUE)).isEqualTo(DoubleLong.from(BigInteger.valueOf(Long.MIN_VALUE).negate()));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "0", "1", "2147483647", "2147483648", "9223372036854775807", "9223372036854775808",
        "-1", "-2147483647", "-2147483648", "-9223372036854775807", "-9223372036854775808",
    })
    public void construction_simple(String num) {
         assertConstruction(num);
    }

    @Test
    public void construction_ultimate() {
        for (BigInteger num : EDGE_CASE_BIG_INTEGERS) {
            assertConstruction(num.toString());
        }
        for (BigInteger num : LARGE_PRIME_NUMBERS) {
            assertConstruction(num.toString());
        }
    }

    private static @NotNull List<DoubleLong> internal_consistency_simple() {
        return TestingBasics.flatListOf(
            DoubleLong.ZERO, DoubleLong.ONE,
            DoubleLong.MIN_VALUE, DoubleLong.MAX_VALUE,
            SIMPLE_MIN_MAX_VALUES.stream().map(DoubleLong::from)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void internal_consistency_simple(DoubleLong val) {
        assertThatDoubleLong(val).internalConsistency();
    }

    @Tag("slow")
    @Test
    public void internal_consistency_ultimate() {
        for (BigInteger num : EDGE_CASE_BIG_INTEGERS) {
            assertThatDoubleLong(DoubleLong.from(num)).internalConsistency();
        }
        for (BigInteger num : LARGE_PRIME_NUMBERS) {
            assertThatDoubleLong(DoubleLong.from(num)).internalConsistency();
        }
        for (long num : EDGE_CASE_LONGS) {
            assertThatDoubleLong(DoubleLong.from(num)).internalConsistency();
        }
    }

    private static final List<DoubleLong> FIT_INTO_LONG =
        List.of(DoubleLong.ZERO, DoubleLong.ONE, DoubleLong.from(Long.MAX_VALUE), DoubleLong.from(Long.MIN_VALUE));
    private static final List<DoubleLong> NOT_FIT_INTO_LONG = List.of(
        DoubleLong.MAX_VALUE, DoubleLong.MIN_VALUE,
        DoubleLong.from("9223372036854775808"), DoubleLong.from("-9223372036854775809")
    );

    @Test
    public void trivial_fits_into_long() {
        assertThat(DoubleLong.ZERO.fitsIntoLong()).isTrue();
        assertThat(DoubleLong.ONE.fitsIntoLong()).isTrue();
        assertThat(DoubleLong.from(Long.MAX_VALUE).fitsIntoLong()).isTrue();
        assertThat(DoubleLong.from(Long.MIN_VALUE).fitsIntoLong()).isTrue();

        assertThat(DoubleLong.MAX_VALUE.fitsIntoLong()).isFalse();
        assertThat(DoubleLong.MIN_VALUE.fitsIntoLong()).isFalse();
        assertThat(DoubleLong.from("9223372036854775808").fitsIntoLong()).isFalse();
        assertThat(DoubleLong.from("-9223372036854775809").fitsIntoLong()).isFalse();
    }

    @Test
    public void multiply_simple() {
        assertThat(DoubleLong.ZERO.multiply(DoubleLong.ZERO)).isEqualTo(DoubleLong.ZERO);
        assertThat(DoubleLong.ZERO.multiply(DoubleLong.ONE)).isEqualTo(DoubleLong.ZERO);
        assertThat(DoubleLong.ONE.multiply(DoubleLong.ONE)).isEqualTo(DoubleLong.ONE);
        assertThat(DoubleLong.ONE.multiply(DoubleLong.from(2))).isEqualTo(DoubleLong.from(2));
        assertThat(DoubleLong.from(2).multiply(DoubleLong.from(2))).isEqualTo(DoubleLong.from(4));
    }

    @Test
    public void multiple_positive() {
        assertMultiplyMatchesBigInteger(
            DoubleLong.fromBits(0, 1L << 36 + 1),
            DoubleLong.fromBits(0, 1L << 36 + 2)
        );
        assertMultiplyMatchesBigInteger(
            DoubleLong.fromBits(1 << 16, 1L << 24 + 1),
            DoubleLong.fromBits(1 << 10, 1L << 18 + 2)
        );
        assertMultiplyMatchesBigInteger(
            DoubleLong.fromBits(1L << 20 + 1, 1L << 30 + 1),
            DoubleLong.fromBits(1L << 20 - 1, 1L << 18 - 1)
        );
        assertMultiplyMatchesBigInteger(
            DoubleLong.fromBits(1L << 30 - 1, 1L << 24 - 1),
            DoubleLong.fromBits(1L << 15 - 1, 1L << 12 - 1)
        );

        assertMultiplyMatchesBigInteger(DoubleLong.from(Integer.MAX_VALUE), DoubleLong.from(Integer.MAX_VALUE));
        assertMultiplyMatchesBigInteger(DoubleLong.from(Long.MAX_VALUE), DoubleLong.from(Long.MAX_VALUE));
    }

    @Test
    public void multiply_negative() {
        assertMultiplyMatchesBigInteger(DoubleLong.from(-1), DoubleLong.from(1));
        assertMultiplyMatchesBigInteger(DoubleLong.from(-1), DoubleLong.from(-1));
        assertMultiplyMatchesBigInteger(DoubleLong.from(Integer.MAX_VALUE), DoubleLong.from(Integer.MIN_VALUE));
        assertMultiplyMatchesBigInteger(DoubleLong.from(Integer.MIN_VALUE), DoubleLong.from(Integer.MIN_VALUE));
        assertMultiplyMatchesBigInteger(DoubleLong.from(Long.MAX_VALUE), DoubleLong.from(Long.MIN_VALUE));
        assertMultiplyMatchesBigInteger(DoubleLong.from(Long.MIN_VALUE), DoubleLong.from(Long.MIN_VALUE));
    }

    @Tag("slow")
    @Test
    public void multiply_ultimate() {
        for (BigInteger a : EDGE_CASE_BIG_INTEGERS) {
            for (BigInteger b : EDGE_CASE_BIG_INTEGERS) {
                assertMultiplyMatchesBigInteger(DoubleLong.from(a), DoubleLong.from(b));
            }
        }
    }

    private static void assertMultiplyMatchesBigInteger(DoubleLong lhs, DoubleLong rhs) {
        BigInteger expected = fitInto128Bits(lhs.toBigInteger().multiply(rhs.toBigInteger()));
        assertThat(lhs.multiply(rhs).toBigInteger()).isEqualTo(expected);
    }

    // Assertion utils

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
                .roundtripUnsignedLong()
                .roundtripLong()
                .fitsIntoLongConsistency()
                .compareMatchesBigInteger()
                .addMatchesBigInteger()
                .subtractMatchesBigInteger()
                .negateMatchesBigInteger()
                .logicOpsMatchesBigInteger()
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

        private @NotNull DoubleLongSubject roundtripUnsignedLong() {
            if (actual.fitsIntoLong()) {
                UnsignedLong unsignedLong = actual.toUnsignedLong();
                DoubleLong copy = DoubleLong.from(unsignedLong);
                return isEqualTo(copy);
            }
            assertThrows(AssertionError.class, () -> actual.toUnsignedLong());
            return this;
        }

        public @NotNull DoubleLongSubject roundtripLong() {
            if (actual.fitsIntoLong()) {
                long parsed = EasyPrimitives.parseLongSafe(actual.toString());
                DoubleLong copy = DoubleLong.from(parsed);
                return isEqualTo(copy);
            }
            return this;
        }

        public @NotNull DoubleLongSubject fitsIntoLongConsistency() {
            boolean isPositiveOrZero = actual.compareTo(DoubleLong.ZERO) >= 0;
            try {
                Long.parseLong(actual.toString());
                assertThat(actual.fitsIntoLong()).isTrue();
                assertThat(actual.highBits()).isEqualTo(isPositiveOrZero ? 0 : -1);
                assertThat(actual.compareTo(DoubleLong.from(Long.MAX_VALUE))).isAnyOf(0, -1);
                assertThat(actual.compareTo(DoubleLong.from(Long.MIN_VALUE))).isAnyOf(0, 1);
            } catch (NumberFormatException e) {
                assertThat(actual.fitsIntoLong()).isFalse();
                assertThat(actual.compareTo(DoubleLong.from(Long.MAX_VALUE))).isEqualTo(isPositiveOrZero ? 1 : -1);
                assertThat(actual.compareTo(DoubleLong.from(Long.MIN_VALUE))).isEqualTo(isPositiveOrZero ? 1 : -1);
            }
            assertThat(actual.fitsIntoLong()).isEqualTo(fitsIntoLong(actual.toBigInteger()));
            return this;
        }

        public @NotNull DoubleLongSubject compareMatchesBigInteger() {
            for (BigInteger big : EDGE_CASE_BIG_INTEGERS) {
                assertThat(actual.compareTo(DoubleLong.from(big))).isEqualTo(actual.toBigInteger().compareTo(big));
                assertThat(DoubleLong.from(big).compareTo(actual)).isEqualTo(big.compareTo(actual.toBigInteger()));
            }
            return this;
        }

        public @NotNull DoubleLongSubject addMatchesBigInteger() {
            for (BigInteger big : EDGE_CASE_BIG_INTEGERS) {
                BigInteger expected = fitInto128Bits(actual.toBigInteger().add(big));
                assertThat(actual.add(DoubleLong.from(big)).toBigInteger()).isEqualTo(expected);
                assertThat(DoubleLong.from(big).add(actual).toBigInteger()).isEqualTo(expected);
                assertThat(DoubleLong.add(actual, DoubleLong.from(big)).toBigInteger()).isEqualTo(expected);
                assertThat(DoubleLong.add(DoubleLong.from(big), actual).toBigInteger()).isEqualTo(expected);
            }
            return this;
        }

        public @NotNull DoubleLongSubject subtractMatchesBigInteger() {
            for (BigInteger big : EDGE_CASE_BIG_INTEGERS) {
                assertThat(actual.subtract(DoubleLong.from(big)).toBigInteger())
                    .isEqualTo(fitInto128Bits(actual.toBigInteger().subtract(big)));
                assertThat(DoubleLong.subtract(actual, DoubleLong.from(big)).toBigInteger())
                    .isEqualTo(fitInto128Bits(actual.toBigInteger().subtract(big)));
                assertThat(DoubleLong.from(big).subtract(actual).toBigInteger())
                    .isEqualTo(fitInto128Bits(big.subtract(actual.toBigInteger())));
                assertThat(DoubleLong.subtract(DoubleLong.from(big), actual).toBigInteger())
                    .isEqualTo(fitInto128Bits(big.subtract(actual.toBigInteger())));
            }
            return this;
        }

        public @NotNull DoubleLongSubject negateMatchesBigInteger() {
            BigInteger expected = fitInto128Bits(actual.toBigInteger().negate());
            assertThat(actual.negate().toBigInteger()).isEqualTo(expected);
            assertThat(DoubleLong.negate(actual).toBigInteger()).isEqualTo(expected);
            return isEqualTo(actual.negate().negate());
        }

        public @NotNull DoubleLongSubject logicOpsMatchesBigInteger() {
            for (BigInteger big : EDGE_CASE_BIG_INTEGERS) {
                assertThat(actual.and(DoubleLong.from(big)).toBigInteger()).isEqualTo(actual.toBigInteger().and(big));
                assertThat(DoubleLong.from(big).and(actual).toBigInteger()).isEqualTo(actual.toBigInteger().and(big));
                assertThat(actual.or(DoubleLong.from(big)).toBigInteger()).isEqualTo(actual.toBigInteger().or(big));
                assertThat(DoubleLong.from(big).or(actual).toBigInteger()).isEqualTo(actual.toBigInteger().or(big));
                assertThat(actual.xor(DoubleLong.from(big)).toBigInteger()).isEqualTo(actual.toBigInteger().xor(big));
                assertThat(DoubleLong.from(big).xor(actual).toBigInteger()).isEqualTo(actual.toBigInteger().xor(big));
            }
            return this;
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
