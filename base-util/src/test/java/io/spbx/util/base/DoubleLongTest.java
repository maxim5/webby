package io.spbx.util.base;

import com.google.common.base.Strings;
import com.google.common.primitives.UnsignedLong;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.spbx.util.collect.ListBuilder;
import io.spbx.util.testing.TestingBasics;
import io.spbx.util.testing.TestingBigIntegers;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static io.spbx.util.testing.TestingBigIntegers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("EqualsWithItself")
public class DoubleLongTest {
    // The last one overflows, it's ok.
    private static final long[] POW2 = LongStream.range(0, 64).map(n -> 1L << n).toArray();

    private static final List<Long> SIMPLE_MIN_MAX_VALUES = Stream.of(
        Byte.MAX_VALUE, Byte.MIN_VALUE,
        Short.MAX_VALUE, Short.MIN_VALUE,
        Integer.MAX_VALUE, Integer.MIN_VALUE,
        Long.MAX_VALUE, Long.MIN_VALUE
    ).map(Number::longValue).toList();

    // Test convenient comparator: first positive, then negative.
    private static final Comparator<BigInteger> CMP = (lhs, rhs) ->
        lhs.signum() >= 0 && rhs.signum() >= 0 ? lhs.compareTo(rhs) : -lhs.compareTo(rhs);

    private static final List<BigInteger> EDGE_CASE_BIG_INTEGERS = IntStream.range(0, 128)
        .mapToObj($1::shiftLeft)
        .flatMap(b -> Stream.of(b, b.subtract($1), b.negate(), b.negate().add($1)))
        .map(TestingBigIntegers::fitIntoSigned128Bits)
        .sorted(CMP).distinct().toList();

    private static final List<Long> EDGE_CASE_LONGS =
        EDGE_CASE_BIG_INTEGERS.stream().map(BigInteger::longValue).distinct().toList();

    // From https://bigprimes.org/
    private static final List<BigInteger> LARGE_PRIME_NUMBERS = Stream.of(
        "477431785618589",
        "7734245186249221",
        "313506233314930897",
        "14262866606244585389",
        "9312117738832437088867",
        "643060765953885798980471",
        "86177578550109096835627979",
        "9789690428521166127696463099",
        "511502705152331397313213352309",
        "37010749720546680375917374542419",
        "9619122375485128076391017781203171",
        "729607297303010430697493914704054547",
        "19008080681165741144368765276016110223",
        "134729416280889832795054608867944616583"
    ).map(BigInteger::new).flatMap(b -> Stream.of(b, b.negate())).sorted(CMP).toList();

    private static final List<BigInteger> BIG_INTEGERS = ListBuilder.concat(EDGE_CASE_BIG_INTEGERS, LARGE_PRIME_NUMBERS);

    /** Constants */

    @Test
    public void const_zero_trivial() {
        assertThat(DoubleLong.ZERO.highBits()).isEqualTo(0);
        assertThat(DoubleLong.ZERO.lowBits()).isEqualTo(0);
        assertThat(DoubleLong.ZERO.toBigInteger()).isEqualTo($0);
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
    public void const_one_trivial() {
        assertThat(DoubleLong.ONE.highBits()).isEqualTo(0);
        assertThat(DoubleLong.ONE.lowBits()).isEqualTo(1);
        assertThat(DoubleLong.ONE.toBigInteger()).isEqualTo($1);
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
    public void const_max_value_trivial() {
        BigInteger expected = INT128_MAX;  // (1 << 127) - 1
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
    public void const_min_value_trivial() {
        BigInteger expected = INT128_MIN;  // -(1 << 127)
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

    /** {@link DoubleLong#fromBits}}, {@link DoubleLong#from}, {@link DoubleLong#fromHex} */

    @Test
    public void construction_from_bits_long_trivial() {
        assertThat(DoubleLong.fromBits(0, 0)).isEqualTo(DoubleLong.from($0));
        assertThat(DoubleLong.fromBits(0, 1)).isEqualTo(DoubleLong.from($1));
        assertThat(DoubleLong.fromBits(0, -1)).isEqualTo(DoubleLong.from(UINT64_MAX));

        assertThat(DoubleLong.fromBits(1, 0)).isEqualTo(DoubleLong.from($2_64));
        assertThat(DoubleLong.fromBits(1, 1)).isEqualTo(DoubleLong.from($2_64.add($1)));
        assertThat(DoubleLong.fromBits(1, -1)).isEqualTo(DoubleLong.from($2_65.subtract($1)));

        assertThat(DoubleLong.fromBits(-1, 0)).isEqualTo(DoubleLong.from($2_64.negate()));
        assertThat(DoubleLong.fromBits(-1, 1)).isEqualTo(DoubleLong.from($2_64.negate().add($1)));
        assertThat(DoubleLong.fromBits(-1, -1)).isEqualTo(DoubleLong.from($1.negate()));

        assertThat(DoubleLong.fromBits(0, Long.MAX_VALUE)).isEqualTo(DoubleLong.from(INT64_MAX));
        assertThat(DoubleLong.fromBits(0, Long.MIN_VALUE)).isEqualTo(DoubleLong.from(INT64_MIN.negate()));
        assertThat(DoubleLong.fromBits(Long.MAX_VALUE, 0)).isEqualTo(DoubleLong.from($2_127.subtract($2_64)));
        assertThat(DoubleLong.fromBits(Long.MIN_VALUE, 0)).isEqualTo(DoubleLong.from(INT128_MIN));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "0", "1", "2147483647", "2147483648", "9223372036854775807", "9223372036854775808",
        "-1", "-2147483647", "-2147483648", "-9223372036854775807", "-9223372036854775808",
    })
    public void construction_from_string_simple(String num) {
        assertConstruction(num);
    }

    @Test
    public void construction_from_string_ultimate() {
        for (BigInteger num : BIG_INTEGERS) {
            assertConstruction(num.toString());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "0", "1", "10", "ff", "7f", "aaa", "abcd", "ffff_ffff"
    })
    public void construction_from_hex_simple_long(String hex) {
        long num = Long.parseLong(hex.replaceAll("_", ""), 16);
        DoubleLong expected = DoubleLong.from(num);

        assertThat(DoubleLong.fromHex(hex)).isEqualTo(expected);
        assertThat(DoubleLong.fromHex(hex.toUpperCase())).isEqualTo(expected);
        assertThat(DoubleLong.fromHex("0x" + hex)).isEqualTo(expected);
        assertThat(DoubleLong.fromHex("0x0000" + hex)).isEqualTo(expected);

        assertThat(DoubleLong.fromHex("-" + hex)).isEqualTo(expected.negate());
        assertThat(DoubleLong.fromHex("-" + hex.toUpperCase())).isEqualTo(expected.negate());
        assertThat(DoubleLong.fromHex("-0x" + hex)).isEqualTo(expected.negate());
        assertThat(DoubleLong.fromHex("-0x0000" + hex)).isEqualTo(expected.negate());
    }

    @Test
    public void construction_from_hex_ultimate() {
        for (BigInteger num : BIG_INTEGERS) {
            assertThat(DoubleLong.fromHex(num.toString(16))).isEqualTo(DoubleLong.from(num));
        }
    }

    /** Internal Consistency */

    @ParameterizedTest
    @MethodSource
    public void internal_consistency_simple(DoubleLong val) {
        assertThatDoubleLong(val).internalConsistency();
    }

    private static @NotNull List<DoubleLong> internal_consistency_simple() {
        return TestingBasics.flatListOf(
            DoubleLong.ZERO, DoubleLong.ONE,
            DoubleLong.MIN_VALUE, DoubleLong.MAX_VALUE,
            SIMPLE_MIN_MAX_VALUES.stream().map(DoubleLong::from)
        );
    }

    @Tag("slow")
    @Test
    public void internal_consistency_ultimate() {
        for (BigInteger num : BIG_INTEGERS) {
            assertThatDoubleLong(DoubleLong.from(num)).internalConsistency();
        }
        for (long num : EDGE_CASE_LONGS) {
            assertThatDoubleLong(DoubleLong.from(num)).internalConsistency();
        }
    }

    /** {@link DoubleLong#increment()} */

    @ParameterizedTest
    @ValueSource(longs = { 0, 1, -1, Integer.MAX_VALUE, Integer.MIN_VALUE, Long.MIN_VALUE, Long.MAX_VALUE - 1 })
    public void increment_simple_long(long num) {
        assertThat(DoubleLong.from(num).increment()).isEqualTo(DoubleLong.from(num + 1));
    }

    @Test
    public void increment_ultimate() {
        for (BigInteger num : BIG_INTEGERS) {
            BigInteger expected = fitIntoSigned128Bits(num.add($1));
            assertThat(DoubleLong.from(num).increment()).isEqualTo(DoubleLong.from(expected));
        }
    }

    /** {@link DoubleLong#decrement()} */

    @ParameterizedTest
    @ValueSource(longs = { 0, 1, -1, Integer.MAX_VALUE, Integer.MIN_VALUE, Long.MIN_VALUE + 1, Long.MAX_VALUE })
    public void decrement_simple_long(long num) {
        assertThat(DoubleLong.from(num).decrement()).isEqualTo(DoubleLong.from(num - 1));
    }

    @Test
    public void decrement_ultimate() {
        for (BigInteger num : BIG_INTEGERS) {
            BigInteger expected = fitIntoSigned128Bits(num.subtract($1));
            assertThat(DoubleLong.from(num).decrement()).isEqualTo(DoubleLong.from(expected));
        }
    }

    /** {@link DoubleLong#fitsIntoLong()} */

    private static final List<DoubleLong> FIT_INTO_LONG =
        List.of(DoubleLong.ZERO, DoubleLong.ONE, DoubleLong.from(Long.MAX_VALUE), DoubleLong.from(Long.MIN_VALUE));
    private static final List<DoubleLong> NOT_FIT_INTO_LONG = List.of(
        DoubleLong.MAX_VALUE, DoubleLong.MIN_VALUE,
        DoubleLong.from("9223372036854775808"), DoubleLong.from("-9223372036854775809")
    );

    @Test
    public void fitsIntoLong_simple() {
        assertThat(DoubleLong.ZERO.fitsIntoLong()).isTrue();
        assertThat(DoubleLong.ONE.fitsIntoLong()).isTrue();
        assertThat(DoubleLong.from(Long.MAX_VALUE).fitsIntoLong()).isTrue();
        assertThat(DoubleLong.from(Long.MIN_VALUE).fitsIntoLong()).isTrue();

        assertThat(DoubleLong.MAX_VALUE.fitsIntoLong()).isFalse();
        assertThat(DoubleLong.MIN_VALUE.fitsIntoLong()).isFalse();
        assertThat(DoubleLong.from("9223372036854775808").fitsIntoLong()).isFalse();
        assertThat(DoubleLong.from("-9223372036854775809").fitsIntoLong()).isFalse();
    }

    /** {@link DoubleLong#multiply(DoubleLong)} */

    @Test
    public void multiply_simple() {
        assertThat(DoubleLong.ZERO.multiply(DoubleLong.ZERO)).isEqualTo(DoubleLong.ZERO);
        assertThat(DoubleLong.ZERO.multiply(DoubleLong.ONE)).isEqualTo(DoubleLong.ZERO);
        assertThat(DoubleLong.ONE.multiply(DoubleLong.ONE)).isEqualTo(DoubleLong.ONE);
        assertThat(DoubleLong.ONE.multiply(DoubleLong.from(2))).isEqualTo(DoubleLong.from(2));
        assertThat(DoubleLong.from(2).multiply(DoubleLong.from(2))).isEqualTo(DoubleLong.from(4));
    }

    @Test
    public void multiply_positive() {
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
        for (BigInteger a : BIG_INTEGERS) {
            for (BigInteger b : BIG_INTEGERS) {
                assertMultiplyMatchesBigInteger(DoubleLong.from(a), DoubleLong.from(b));
            }
        }
    }

    private static void assertMultiplyMatchesBigInteger(@NotNull DoubleLong lhs, @NotNull DoubleLong rhs) {
        BigInteger expected = fitIntoSigned128Bits(lhs.toBigInteger().multiply(rhs.toBigInteger()));
        assertThat(lhs.multiply(rhs).toBigInteger()).isEqualTo(expected);
    }

    /** {@link DoubleLong#divide(DoubleLong)} */

    private static final BigInteger $2_20 = $(1L << 20);                            // (1 << 20)
    private static final BigInteger $2_36 = $(1L << 36);                            // (1 << 36)
    private static final BigInteger $2_48 = $(1L << 48);                            // (1 << 48)
    private static final BigInteger $2_61 = $(1L << 61);                            // (1 << 61)
    private static final BigInteger $2_62 = $(1L << 62);                            // (1 << 62)
    private static final BigInteger $2_65 = $1.shiftLeft(65);                       // (1 << 65)
    private static final List<BigInteger> SIMPLE_POSITIVE_INTEGERS = List.of(
        $0, $1, $2, $10, $(20), $(100_000), $(100_000_000),
        INT8_MAX, INT16_MAX, INT32_MAX, INT64_MAX, UINT8_MAX, UINT16_MAX, UINT32_MAX,
        $2_20.subtract($1), $2_36, $2_36.add($1), $2_36.subtract($1), $2_48, $2_61, $2_62, $2_63
    );
    private static final List<BigInteger> SIMPLE_NEGATIVE_INTEGERS =
        SIMPLE_POSITIVE_INTEGERS.stream().map(BigInteger::negate).toList();

    @Test
    public void divide_simple_positive_only() {
        for (BigInteger a : SIMPLE_POSITIVE_INTEGERS) {
            for (BigInteger b : SIMPLE_POSITIVE_INTEGERS) {
                assertDivideMatchesBigInteger(DoubleLong.from(a), DoubleLong.from(b));
            }
        }
    }

    @Test
    public void divide_simple_negative_only() {
        for (BigInteger a : SIMPLE_POSITIVE_INTEGERS) {
            for (BigInteger b : SIMPLE_POSITIVE_INTEGERS) {
                assertDivideMatchesBigInteger(DoubleLong.from(a), DoubleLong.from(b));
            }
        }
    }

    @Test
    public void divide_simple_positive_and_negative() {
        List<BigInteger> list = ListBuilder.concat(SIMPLE_POSITIVE_INTEGERS, SIMPLE_NEGATIVE_INTEGERS);
        for (BigInteger a : list) {
            for (BigInteger b : list) {
                assertDivideMatchesBigInteger(DoubleLong.from(a), DoubleLong.from(b));
            }
        }
    }

    @Tag("slow")
    @Test
    public void divide_ultimate() {
        for (BigInteger a : BIG_INTEGERS) {
            for (BigInteger b : BIG_INTEGERS) {
                assertDivideMatchesBigInteger(DoubleLong.from(a), DoubleLong.from(b));
            }
        }
    }

    private static void assertDivideMatchesBigInteger(DoubleLong lhs, DoubleLong rhs) {
        if (rhs.equals(DoubleLong.ZERO)) {
            assertThrows(AssertionError.class, () -> lhs.divide(rhs));
        } else {
            BigInteger expected = fitIntoSigned128Bits(lhs.toBigInteger().divide(rhs.toBigInteger()));
            assertThat(lhs.divide(rhs).toBigInteger()).isEqualTo(expected);
        }
    }

    /** {@link DoubleLong#negate()} */

    @Test
    public void negate_simple() {
        assertThat(DoubleLong.ZERO.negate()).isEqualTo(DoubleLong.ZERO);
        assertThat(DoubleLong.ONE.negate()).isEqualTo(DoubleLong.from(-1));
        assertThat(DoubleLong.MAX_VALUE.negate()).isEqualTo(DoubleLong.MIN_VALUE.add(DoubleLong.ONE));
        assertThat(DoubleLong.MIN_VALUE.negate()).isEqualTo(DoubleLong.MIN_VALUE);  // Same!!!
    }

    @Test
    public void negate_ultimate() {
        for (BigInteger num : BIG_INTEGERS) {
            BigInteger expected = fitIntoSigned128Bits(num.negate());
            assertThat(DoubleLong.from(num).negate().toBigInteger()).isEqualTo(expected);
        }
    }

    /** {@link DoubleLong#shiftLeft(int)} */

    @Test
    public void shiftLeft_simple() {
        assertThat(DoubleLong.ONE.shiftLeft(5)).isEqualTo(DoubleLong.from(POW2[5]));
        assertThat(DoubleLong.ONE.shiftLeft(4)).isEqualTo(DoubleLong.from(POW2[4]));
        assertThat(DoubleLong.ONE.shiftLeft(2)).isEqualTo(DoubleLong.from(POW2[2]));
        assertThat(DoubleLong.ONE.shiftLeft(1)).isEqualTo(DoubleLong.from(POW2[1]));
        assertThat(DoubleLong.ONE.shiftLeft(0)).isEqualTo(DoubleLong.ONE);

        assertThat(DoubleLong.fromBits(1,  1).shiftLeft(5)).isEqualTo(DoubleLong.fromBits(POW2[5], POW2[5]));
        assertThat(DoubleLong.fromBits(0, -1).shiftLeft(5)).isEqualTo(DoubleLong.fromBits(POW2[5] - 1, -POW2[5]));
        assertThat(DoubleLong.fromBits(0, -1).shiftLeft(8)).isEqualTo(DoubleLong.fromBits(POW2[8] - 1, -POW2[8]));
        assertThat(DoubleLong.fromBits(0, -1).shiftLeft(20)).isEqualTo(DoubleLong.fromBits(POW2[20] - 1, -POW2[20]));
        assertThat(DoubleLong.fromBits(0, -1).shiftLeft(60)).isEqualTo(DoubleLong.fromBits(POW2[60] - 1, -POW2[60]));
        assertThat(DoubleLong.fromBits(0, -1).shiftLeft(63)).isEqualTo(DoubleLong.fromBits(POW2[63] - 1, -POW2[63]));
    }

    @Test
    public void shiftLeft_ultimate() {
        for (BigInteger bigInteger : BIG_INTEGERS) {
            assertShiftLeftMatchesBigInteger(bigInteger, 1);
            assertShiftLeftMatchesBigInteger(bigInteger, 10);
            assertShiftLeftMatchesBigInteger(bigInteger, 32);
            assertShiftLeftMatchesBigInteger(bigInteger, 63);
            assertShiftLeftMatchesBigInteger(bigInteger, 64);
            assertShiftLeftMatchesBigInteger(bigInteger, 90);
            assertShiftLeftMatchesBigInteger(bigInteger, 127);
        }
    }

    private static void assertShiftLeftMatchesBigInteger(BigInteger num, int len) {
        BigInteger expected = fitIntoSigned128Bits(num.shiftLeft(len));
        assertThat(DoubleLong.from(num).shiftLeft(len).toBigInteger()).isEqualTo(expected);
    }

    /** {@link DoubleLong#shiftRight(int)} */

    @Test
    public void shiftRight_simple() {
        assertThat(DoubleLong.ONE.shiftRight(0)).isEqualTo(DoubleLong.ONE);
        assertThat(DoubleLong.ONE.shiftRight(1)).isEqualTo(DoubleLong.ZERO);

        assertThat(DoubleLong.from(POW2[5]).shiftRight(4)).isEqualTo(DoubleLong.from(POW2[1]));
        assertThat(DoubleLong.from(POW2[5]).shiftRight(5)).isEqualTo(DoubleLong.ONE);

        assertThat(DoubleLong.fromBits(1, 1).shiftRight(1)).isEqualTo(DoubleLong.fromBits(0, POW2[63]));
        assertThat(DoubleLong.fromBits(3, 3).shiftRight(1)).isEqualTo(DoubleLong.fromBits(1, POW2[63] + 1));
    }

    @Test
    public void shiftRight_ultimate() {
        for (BigInteger bigInteger : BIG_INTEGERS) {
            assertShiftRightMatchesBigInteger(bigInteger, 1);
            assertShiftRightMatchesBigInteger(bigInteger, 10);
            assertShiftRightMatchesBigInteger(bigInteger, 32);
            assertShiftRightMatchesBigInteger(bigInteger, 63);
            assertShiftRightMatchesBigInteger(bigInteger, 64);
            assertShiftRightMatchesBigInteger(bigInteger, 90);
            assertShiftRightMatchesBigInteger(bigInteger, 127);
        }
    }

    private static void assertShiftRightMatchesBigInteger(BigInteger num, int len) {
        BigInteger expected = fitIntoSigned128Bits(num.shiftRight(len));
        assertThat(DoubleLong.from(num).shiftRight(len).toBigInteger()).isEqualTo(expected);
    }

    /** Assertion utils */

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
            assertThat(actual.fitsIntoLong()).isEqualTo(isFitsIntoSignedLong(actual.toBigInteger()));
            return this;
        }

        public @NotNull DoubleLongSubject compareMatchesBigInteger() {
            for (BigInteger big : BIG_INTEGERS) {
                assertThat(actual.compareTo(DoubleLong.from(big))).isEqualTo(actual.toBigInteger().compareTo(big));
                assertThat(DoubleLong.from(big).compareTo(actual)).isEqualTo(big.compareTo(actual.toBigInteger()));
            }
            return this;
        }

        public @NotNull DoubleLongSubject addMatchesBigInteger() {
            for (BigInteger big : BIG_INTEGERS) {
                BigInteger expected = fitIntoSigned128Bits(actual.toBigInteger().add(big));
                assertThat(actual.add(DoubleLong.from(big)).toBigInteger()).isEqualTo(expected);
                assertThat(DoubleLong.from(big).add(actual).toBigInteger()).isEqualTo(expected);
            }
            return this;
        }

        public @NotNull DoubleLongSubject subtractMatchesBigInteger() {
            for (BigInteger big : BIG_INTEGERS) {
                assertThat(actual.subtract(DoubleLong.from(big)).toBigInteger())
                    .isEqualTo(fitIntoSigned128Bits(actual.toBigInteger().subtract(big)));
                assertThat(DoubleLong.from(big).subtract(actual).toBigInteger())
                    .isEqualTo(fitIntoSigned128Bits(big.subtract(actual.toBigInteger())));
            }
            return this;
        }

        public @NotNull DoubleLongSubject negateMatchesBigInteger() {
            BigInteger expected = fitIntoSigned128Bits(actual.toBigInteger().negate());
            assertThat(actual.negate().toBigInteger()).isEqualTo(expected);
            return isEqualTo(actual.negate().negate());
        }

        public @NotNull DoubleLongSubject logicOpsMatchesBigInteger() {
            for (BigInteger big : BIG_INTEGERS) {
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

        if (isFitsIntoSignedLong(bigInteger)) {
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

        if (bigInteger.compareTo($0) >= 0) {
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

        assertThat(value.equals(num)).isTrue();
        assertThat(value.equals(~num)).isFalse();
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
        assertThat(value.toBigInteger()).isEqualTo($((long) num));
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
