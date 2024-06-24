package io.spbx.util.base;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
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
        assertThrows(AssertionError.class, () -> EasyPrimitives.requirePositive(0));
        assertThrows(AssertionError.class, () -> EasyPrimitives.requirePositive(-1));
    }

    @Test
    public void requireNonNegative_simple() {
        assertThat(EasyPrimitives.requireNonNegative(0)).isEqualTo(0);
        assertThat(EasyPrimitives.requireNonNegative(1)).isEqualTo(1);
        assertThat(EasyPrimitives.requireNonNegative(Integer.MAX_VALUE)).isEqualTo(Integer.MAX_VALUE);
        assertThrows(AssertionError.class, () -> EasyPrimitives.requirePositive(-1));
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
}
