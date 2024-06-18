package io.spbx.util.base;

import io.spbx.util.base.EasyPrimitives;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        assertThat(EasyPrimitives.parseIntSafe("123", -1)).isEqualTo(123);
        assertThat(EasyPrimitives.parseIntSafe("-123", -1)).isEqualTo(-123);
        assertThat(EasyPrimitives.parseIntSafe("0", -1)).isEqualTo(0);
        assertThat(EasyPrimitives.parseIntSafe("0.0", -1)).isEqualTo(-1);
        assertThat(EasyPrimitives.parseIntSafe("foo", -1)).isEqualTo(-1);
        assertThat(EasyPrimitives.parseIntSafe("", -1)).isEqualTo(-1);
    }
}
