package io.spbx.util.base;

import com.google.common.base.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap;

import static com.google.common.truth.Truth.assertThat;
import static io.spbx.util.testing.TestingBasics.array;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PairTest {
    @Test
    public void pair_simple_constructors() {
        assertPair(Pair.of(1, 2), 1, 2);
        assertPair(Pair.of(1, "2"), 1, "2");
        assertPair(Pair.of(array(1, 2)), 1, 2);
        assertPair(Pair.of(new AbstractMap.SimpleEntry<>(1, "2")), 1, "2");
    }
    
    @Test
    public void pair_simple_nulls() {
        assertPair(Pair.of(1, null), 1, null);
        assertPair(Pair.of(null, 2), null, 2);
        assertPair(Pair.of(null, null), null, null);

        assertPair(Pair.of(array(null, null)), null, null);
        assertPair(Pair.of(new AbstractMap.SimpleEntry<>(null, null)), null, null);
    }

    @Test
    public void pair_invalid() {
        assertThrows(AssertionError.class, () -> Pair.of(array()));
        assertThrows(AssertionError.class, () -> Pair.of(array(1)));
        assertThrows(AssertionError.class, () -> Pair.of(array(1, 2, 3)));
    }

    @Test
    public void pair_simple_swap() {
        assertPair(Pair.of(1, 2).swap(), 2, 1);
        assertPair(Pair.of(1, "2").swap(), "2", 1);
        assertPair(Pair.of(1, null).swap(), null, 1);
        assertPair(Pair.of(null, 2).swap(), 2, null);
        assertPair(Pair.of(null, null).swap(), null, null);
    }

    @Test
    public void pair_map() {
        assertPair(Pair.of(1, "2").map(x -> -x, String::length), -1, 1);
        assertPair(Pair.of(1, "2").mapFirst(x -> -x), -1, "2");
        assertPair(Pair.of(1, "2").mapSecond(String::length), 1, 1);
    }

    @Test
    public void pair_map_nullable() {
        assertPair(Pair.of(1, null).map(x -> -x, x -> x), -1, null);
        assertPair(Pair.of(1, null).map(x -> -x, String::valueOf), -1, "null");

        assertPair(Pair.of(null, 2).mapFirst(String::valueOf), "null", 2);
        assertPair(Pair.of(null, 2).mapSecond(String::valueOf), null, "2");

        assertPair(Pair.of(null, null).mapFirst(String::valueOf), "null", null);
        assertPair(Pair.of(null, null).mapSecond(String::valueOf), null, "null");
        assertPair(Pair.of(null, null).map(String::valueOf, String::valueOf), "null", "null");
    }

    @Test
    public void pair_mapToObj() {
        assertThat(Pair.of("1", "2").<String>mapToObj("%s%s"::formatted)).isEqualTo("12");
        assertThat(Pair.of("1", null).<String>mapToObj("%s%s"::formatted)).isEqualTo("1null");
        assertThat(Pair.of(null, "2").<String>mapToObj("%s%s"::formatted)).isEqualTo("null2");
        assertThat(Pair.of(null, null).<String>mapToObj("%s%s"::formatted)).isEqualTo("nullnull");
    }

    @Test
    public void pair_mapToInt() {
        assertThat(Pair.of(1, 2).mapToInt(Integer::sum)).isEqualTo(3);
        assertThat(Pair.of(1, null).mapToInt(Objects::hashCode)).isEqualTo(Objects.hashCode(1, null));
        assertThat(Pair.of(null, 2).mapToInt(Objects::hashCode)).isEqualTo(Objects.hashCode(null, 2));
        assertThat(Pair.of(null, null).mapToInt(Objects::hashCode)).isEqualTo(Objects.hashCode(null, null));
    }

    @Test
    public void pair_mapToLong() {
        assertThat(Pair.of(1, 2).mapToLong(Integer::sum)).isEqualTo(3);
        assertThat(Pair.of(1, null).mapToLong(Objects::hashCode)).isEqualTo(Objects.hashCode(1, null));
        assertThat(Pair.of(null, 2).mapToLong(Objects::hashCode)).isEqualTo(Objects.hashCode(null, 2));
        assertThat(Pair.of(null, null).mapToLong(Objects::hashCode)).isEqualTo(Objects.hashCode(null, null));
    }

    @Test
    public void pair_mapToDouble() {
        assertThat(Pair.of(1, 2).mapToDouble(Integer::sum)).isEqualTo(3);
        assertThat(Pair.of(1, null).mapToDouble(Objects::hashCode)).isEqualTo(Objects.hashCode(1, null));
        assertThat(Pair.of(null, 2).mapToDouble(Objects::hashCode)).isEqualTo(Objects.hashCode(null, 2));
        assertThat(Pair.of(null, null).mapToDouble(Objects::hashCode)).isEqualTo(Objects.hashCode(null, null));
    }

    @Test
    public void pair_testFirst() {
        assertThat(Pair.of(1, 2).testFirst(x -> x == 1)).isTrue();
        assertThat(Pair.of(1, 2).testFirst(x -> x != 1)).isFalse();
        assertThat(Pair.of(null, 2).testFirst(java.util.Objects::isNull)).isTrue();
        assertThat(Pair.of(null, 2).testFirst(java.util.Objects::nonNull)).isFalse();
    }

    @Test
    public void pair_testSecond() {
        assertThat(Pair.of(1, 2).testSecond(x -> x == 2)).isTrue();
        assertThat(Pair.of(1, 2).testSecond(x -> x != 2)).isFalse();
        assertThat(Pair.of(1, null).testSecond(java.util.Objects::isNull)).isTrue();
        assertThat(Pair.of(1, null).testSecond(java.util.Objects::nonNull)).isFalse();
    }

    @Test
    public void pair_test() {
        assertThat(Pair.of(1, 1).test((x, y) -> x - y == 0)).isTrue();
        assertThat(Pair.of(1, 2).test((x, y) -> x - y == 0)).isFalse();
        assertThat(Pair.of(1, null).test((x, y) -> x != null && y == null)).isTrue();
        assertThat(Pair.of(null, 1).test((x, y) -> x == null && y != null)).isTrue();
        assertThat(Pair.of(null, null).test((x, y) -> x == null && y == null)).isTrue();
    }

    private static <U, V> void assertPair(@NotNull Pair<U, V> pair, @Nullable Object first, @Nullable Object second) {
        assertThat(pair.first()).isEqualTo(first);
        assertThat(pair.getKey()).isEqualTo(first);
        assertThat(pair.second()).isEqualTo(second);
        assertThat(pair.getValue()).isEqualTo(second);
    }
}
