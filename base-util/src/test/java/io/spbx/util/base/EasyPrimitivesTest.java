package io.spbx.util.base;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static io.spbx.util.testing.TestingPrimitives.bytes;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("fast")
public class EasyPrimitivesTest {
    @Test
    public void firstNonNegative_of_two() {
        assertThat(EasyPrimitives.firstNonNegative(1, 2)).isEqualTo(1);
        assertThat(EasyPrimitives.firstNonNegative(0, 1)).isEqualTo(0);
        assertThat(EasyPrimitives.firstNonNegative(0, -1)).isEqualTo(0);
        assertThat(EasyPrimitives.firstNonNegative(-1, 0)).isEqualTo(0);
        assertThrows(IllegalArgumentException.class, () -> EasyPrimitives.firstNonNegative(-1, -2));
    }

    @Test
    public void firstNonNegative_of_three() {
        assertThat(EasyPrimitives.firstNonNegative(1, 2, 3)).isEqualTo(1);
        assertThat(EasyPrimitives.firstNonNegative(0, 1, 2)).isEqualTo(0);
        assertThat(EasyPrimitives.firstNonNegative(0, -1, -2)).isEqualTo(0);
        assertThat(EasyPrimitives.firstNonNegative(-1, 0, 1)).isEqualTo(0);
        assertThat(EasyPrimitives.firstNonNegative(-1, 0, -2)).isEqualTo(0);
        assertThat(EasyPrimitives.firstNonNegative(-1, -2, 0)).isEqualTo(0);
        assertThrows(IllegalArgumentException.class, () -> EasyPrimitives.firstNonNegative(-1, -2, -1));
    }

    @Test
    public void firstNonNegative_of_vararg() {
        assertThat(EasyPrimitives.firstNonNegative(1, 2, 3, 4, 5)).isEqualTo(1);
        assertThat(EasyPrimitives.firstNonNegative(0, 1, 2, 0, 1)).isEqualTo(0);
        assertThat(EasyPrimitives.firstNonNegative(0, -1, -2, -3, -4)).isEqualTo(0);
        assertThat(EasyPrimitives.firstNonNegative(-1, 0, -2, -3, -4)).isEqualTo(0);
        assertThat(EasyPrimitives.firstNonNegative(-1, -2, 0, -3, -4)).isEqualTo(0);
        assertThat(EasyPrimitives.firstNonNegative(-1, -2, -3, 0, -4)).isEqualTo(0);
        assertThat(EasyPrimitives.firstNonNegative(-1, -2, -3, -4, 0)).isEqualTo(0);
        assertThrows(IllegalArgumentException.class, () -> EasyPrimitives.firstNonNegative(-1, -2, -1, -2, -3));
    }

    @Test
    public void requirePositive_simple() {
        assertThat(EasyPrimitives.requirePositive(1)).isEqualTo(1);
        assertThat(EasyPrimitives.requirePositive(Integer.MAX_VALUE)).isEqualTo(Integer.MAX_VALUE);
        assertThrows(IllegalArgumentException.class, () -> EasyPrimitives.requirePositive(0));
        assertThrows(IllegalArgumentException.class, () -> EasyPrimitives.requirePositive(-1));
    }

    @Test
    public void requireNonNegative_simple() {
        assertThat(EasyPrimitives.requireNonNegative(0)).isEqualTo(0);
        assertThat(EasyPrimitives.requireNonNegative(1)).isEqualTo(1);
        assertThat(EasyPrimitives.requireNonNegative(Integer.MAX_VALUE)).isEqualTo(Integer.MAX_VALUE);
        assertThrows(IllegalArgumentException.class, () -> EasyPrimitives.requirePositive(-1));
    }

    @Test
    public void parseIntSafe_simple() {
        assertThat(EasyPrimitives.parseIntSafe("0", -1)).isEqualTo(0);
        assertThat(EasyPrimitives.parseIntSafe("123", -1)).isEqualTo(123);
        assertThat(EasyPrimitives.parseIntSafe("-123", -1)).isEqualTo(-123);

        assertThat(EasyPrimitives.parseIntSafe("2147483647", -1)).isEqualTo(Integer.MAX_VALUE);
        assertThat(EasyPrimitives.parseIntSafe("-2147483648", -1)).isEqualTo(Integer.MIN_VALUE);
        assertThat(EasyPrimitives.parseIntSafe("2147483648", -1)).isEqualTo(-1);
        assertThat(EasyPrimitives.parseIntSafe("-2147483649", -1)).isEqualTo(-1);

        assertThat(EasyPrimitives.parseIntSafe("", -1)).isEqualTo(-1);
        assertThat(EasyPrimitives.parseIntSafe("0.", -1)).isEqualTo(-1);
        assertThat(EasyPrimitives.parseIntSafe("0.0", -1)).isEqualTo(-1);
        assertThat(EasyPrimitives.parseIntSafe("1e123", -1)).isEqualTo(-1);
        assertThat(EasyPrimitives.parseIntSafe("foo", -1)).isEqualTo(-1);
    }

    @Test
    public void parseLongSafe_simple() {
        assertThat(EasyPrimitives.parseLongSafe("0", -1)).isEqualTo(0);
        assertThat(EasyPrimitives.parseLongSafe("123", -1)).isEqualTo(123);
        assertThat(EasyPrimitives.parseLongSafe("-123", -1)).isEqualTo(-123);

        assertThat(EasyPrimitives.parseLongSafe("2147483647", -1)).isEqualTo(Integer.MAX_VALUE);
        assertThat(EasyPrimitives.parseLongSafe("-2147483648", -1)).isEqualTo(Integer.MIN_VALUE);
        assertThat(EasyPrimitives.parseLongSafe("2147483648", -1)).isEqualTo(2147483648L);
        assertThat(EasyPrimitives.parseLongSafe("-2147483649", -1)).isEqualTo(-2147483649L);
        assertThat(EasyPrimitives.parseLongSafe("9223372036854775807", -1)).isEqualTo(Long.MAX_VALUE);
        assertThat(EasyPrimitives.parseLongSafe("9223372036854775807L", -1)).isEqualTo(-1);
        assertThat(EasyPrimitives.parseLongSafe("9223372036854775808", -1)).isEqualTo(-1);
        assertThat(EasyPrimitives.parseLongSafe("-9223372036854775808", -1)).isEqualTo(Long.MIN_VALUE);
        assertThat(EasyPrimitives.parseLongSafe("-9223372036854775808L", -1)).isEqualTo(-1);
        assertThat(EasyPrimitives.parseLongSafe("-9223372036854775809", -1)).isEqualTo(-1);

        assertThat(EasyPrimitives.parseLongSafe("", -1)).isEqualTo(-1);
        assertThat(EasyPrimitives.parseLongSafe("0.", -1)).isEqualTo(-1);
        assertThat(EasyPrimitives.parseLongSafe("0.0", -1)).isEqualTo(-1);
        assertThat(EasyPrimitives.parseLongSafe("1e123", -1)).isEqualTo(-1);
        assertThat(EasyPrimitives.parseLongSafe("foo", -1)).isEqualTo(-1);
    }

    @Test
    public void parseByteSafe_simple() {
        byte def = -1;

        assertThat(EasyPrimitives.parseByteSafe("0", def)).isEqualTo(0);
        assertThat(EasyPrimitives.parseByteSafe("123", def)).isEqualTo(123);
        assertThat(EasyPrimitives.parseByteSafe("-123", def)).isEqualTo(-123);

        assertThat(EasyPrimitives.parseByteSafe("127", def)).isEqualTo(Byte.MAX_VALUE);
        assertThat(EasyPrimitives.parseByteSafe("-128", def)).isEqualTo(Byte.MIN_VALUE);
        assertThat(EasyPrimitives.parseByteSafe("128", def)).isEqualTo(def);
        assertThat(EasyPrimitives.parseByteSafe("-129", def)).isEqualTo(def);

        assertThat(EasyPrimitives.parseByteSafe("", def)).isEqualTo(def);
        assertThat(EasyPrimitives.parseByteSafe("0.", def)).isEqualTo(def);
        assertThat(EasyPrimitives.parseByteSafe("0.0", def)).isEqualTo(def);
        assertThat(EasyPrimitives.parseByteSafe("255", def)).isEqualTo(def);
        assertThat(EasyPrimitives.parseByteSafe("1e123", def)).isEqualTo(def);
        assertThat(EasyPrimitives.parseByteSafe("foo", def)).isEqualTo(def);
    }

    @Test
    public void parseBoolSafe_simple() {
        assertThat(EasyPrimitives.parseBoolSafe("true", false)).isTrue();
        assertThat(EasyPrimitives.parseBoolSafe("True", false)).isTrue();
        assertThat(EasyPrimitives.parseBoolSafe("TRUE", false)).isTrue();

        assertThat(EasyPrimitives.parseBoolSafe("false", true)).isFalse();
        assertThat(EasyPrimitives.parseBoolSafe("False", true)).isFalse();
        assertThat(EasyPrimitives.parseBoolSafe("FALSE", true)).isFalse();

        assertThat(EasyPrimitives.parseBoolSafe("0")).isFalse();
        assertThat(EasyPrimitives.parseBoolSafe("1")).isFalse();
        assertThat(EasyPrimitives.parseBoolSafe("no")).isFalse();
        assertThat(EasyPrimitives.parseBoolSafe("yes")).isFalse();

        assertThat(EasyPrimitives.parseBoolSafe("0", true)).isTrue();
        assertThat(EasyPrimitives.parseBoolSafe("1", false)).isFalse();
        assertThat(EasyPrimitives.parseBoolSafe("no", true)).isTrue();
        assertThat(EasyPrimitives.parseBoolSafe("yes", false)).isFalse();
    }

    @Test
    public void parseDoubleSafe_simple() {
        assertThat(EasyPrimitives.parseDoubleSafe("0", -1)).isEqualTo(0.0);
        assertThat(EasyPrimitives.parseDoubleSafe("0.", -1)).isEqualTo(0.0);
        assertThat(EasyPrimitives.parseDoubleSafe("0.0", -1)).isEqualTo(0.0);
        assertThat(EasyPrimitives.parseDoubleSafe("123", -1)).isEqualTo(123);
        assertThat(EasyPrimitives.parseDoubleSafe("-123", -1)).isEqualTo(-123);
        assertThat(EasyPrimitives.parseDoubleSafe("1e123", -1)).isEqualTo(1e123);

        assertThat(EasyPrimitives.parseDoubleSafe("2147483647", -1)).isEqualTo(2147483647.0);
        assertThat(EasyPrimitives.parseDoubleSafe("-2147483648", -1)).isEqualTo(-2147483648.0);
        assertThat(EasyPrimitives.parseDoubleSafe("2147483648", -1)).isEqualTo(2147483648.0);
        assertThat(EasyPrimitives.parseDoubleSafe("-2147483649", -1)).isEqualTo(-2147483649.0);

        assertThat(EasyPrimitives.parseDoubleSafe("", -1)).isEqualTo(-1);
        assertThat(EasyPrimitives.parseDoubleSafe("foo", -1)).isEqualTo(-1);
    }

    @Test
    public void parseFloatSafe_simple() {
        assertThat(EasyPrimitives.parseFloatSafe("0", -1)).isEqualTo(0.0f);
        assertThat(EasyPrimitives.parseFloatSafe("0.", -1)).isEqualTo(0.0f);
        assertThat(EasyPrimitives.parseFloatSafe("0.0", -1)).isEqualTo(0.0f);
        assertThat(EasyPrimitives.parseFloatSafe("123", -1)).isEqualTo(123f);
        assertThat(EasyPrimitives.parseFloatSafe("-123", -1)).isEqualTo(-123f);
        assertThat(EasyPrimitives.parseFloatSafe("1e123", -1)).isPositiveInfinity();

        assertThat(EasyPrimitives.parseFloatSafe("2147483647", -1)).isEqualTo(2147483647.0f);
        assertThat(EasyPrimitives.parseFloatSafe("-2147483648", -1)).isEqualTo(-2147483648.0f);
        assertThat(EasyPrimitives.parseFloatSafe("2147483648", -1)).isEqualTo(2147483648.0f);
        assertThat(EasyPrimitives.parseFloatSafe("-2147483649", -1)).isEqualTo(-2147483649.0f);

        assertThat(EasyPrimitives.parseFloatSafe("", -1)).isEqualTo(-1f);
        assertThat(EasyPrimitives.parseFloatSafe("foo", -1)).isEqualTo(-1f);
    }

    @Test
    public void toByteArray_simple() {
        assertThat(Stream.<Integer>of().collect(EasyPrimitives.toByteArray())).isEmpty();
        assertThat(Stream.of(1).collect(EasyPrimitives.toByteArray())).isEqualTo(bytes(1));
        assertThat(Stream.of(1, 2).collect(EasyPrimitives.toByteArray())).isEqualTo(bytes(1, 2));
        assertThat(IntStream.of().boxed().collect(EasyPrimitives.toByteArray())).isEmpty();
        assertThat(IntStream.of(1).boxed().collect(EasyPrimitives.toByteArray())).isEqualTo(bytes(1));
        assertThat(IntStream.of(1, 2).boxed().collect(EasyPrimitives.toByteArray())).isEqualTo(bytes(1, 2));
    }
}
