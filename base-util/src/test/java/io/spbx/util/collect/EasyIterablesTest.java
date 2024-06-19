package io.spbx.util.collect;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static io.spbx.util.testing.TestingBasics.iterable;
import static io.spbx.util.testing.TestingBasics.sortedSetOf;

public class EasyIterablesTest {
    @Test
    public void asList_list() {
        assertThat(EasyIterables.asList(List.of())).isEmpty();
        assertThat(EasyIterables.asList(List.of(1))).containsExactly(1).inOrder();
        assertThat(EasyIterables.asList(List.of(1, 2))).containsExactly(1, 2).inOrder();
    }

    @Test
    public void asList_list_same_instance() {
        List<Integer> items = List.of(1, 2);
        assertThat(EasyIterables.asList(items)).isSameInstanceAs(items);
    }

    @Test
    public void asList_collection() {
        assertThat(EasyIterables.asList(sortedSetOf())).isEmpty();
        assertThat(EasyIterables.asList(sortedSetOf(1))).containsExactly(1).inOrder();
        assertThat(EasyIterables.asList(sortedSetOf(1, 2))).containsExactly(1, 2).inOrder();
    }

    @Test
    public void asList_iterable() {
        assertThat(EasyIterables.asList(iterable())).isEmpty();
        assertThat(EasyIterables.asList(iterable("foo"))).containsExactly("foo").inOrder();
        assertThat(EasyIterables.asList(iterable("foo", "bar"))).containsExactly("foo", "bar").inOrder();
    }

    @Test
    public void estimateSize_collection() {
        assertThat(EasyIterables.estimateSize(List.of(), -1)).isEqualTo(0);
        assertThat(EasyIterables.estimateSize(List.of(1), -1)).isEqualTo(1);
        assertThat(EasyIterables.estimateSize(List.of(1, 2), -1)).isEqualTo(2);
    }

    @Test
    public void allEqual() {
        assertThat(EasyIterables.allEqual(Stream.of())).isTrue();
        assertThat(EasyIterables.allEqual(Stream.of(1))).isTrue();
        assertThat(EasyIterables.allEqual(Stream.of(1, 1))).isTrue();
        assertThat(EasyIterables.allEqual(Stream.of(1, 1, 1))).isTrue();
        assertThat(EasyIterables.allEqual(Stream.of(1, 2))).isFalse();
        assertThat(EasyIterables.allEqual(Stream.of(1, 2, 1))).isFalse();
        assertThat(EasyIterables.allEqual(Stream.of(1, 2, 2))).isFalse();
        assertThat(EasyIterables.allEqual(Stream.of(1, 1, 2))).isFalse();
    }

    @Test
    public void getOnlyItemOrEmpty_simple() {
        assertThat(Stream.of().collect(EasyIterables.getOnlyItemOrEmpty())).isEmpty();
        assertThat(Stream.of(1).collect(EasyIterables.getOnlyItemOrEmpty())).hasValue(1);
        assertThat(Stream.of(1, 2).collect(EasyIterables.getOnlyItemOrEmpty())).isEmpty();
        assertThat(Stream.of(1, 2, 3).collect(EasyIterables.getOnlyItemOrEmpty())).isEmpty();
        assertThat(Stream.of(1, 2, 3, 4).collect(EasyIterables.getOnlyItemOrEmpty())).isEmpty();
    }

    @Test
    public void getOnlyItemOrEmpty_all_nulls() {
        assertThat(Stream.of((Object) null).collect(EasyIterables.getOnlyItemOrEmpty())).isEmpty();
        assertThat(Stream.of(null, null).collect(EasyIterables.getOnlyItemOrEmpty())).isEmpty();
        assertThat(Stream.of(null, null, null).collect(EasyIterables.getOnlyItemOrEmpty())).isEmpty();
    }

    @Test
    public void getOnlyItemOrEmpty_nulls_and_one_value() {
        assertThat(Stream.of(1, null).collect(EasyIterables.getOnlyItemOrEmpty())).hasValue(1);
        assertThat(Stream.of(null, 1).collect(EasyIterables.getOnlyItemOrEmpty())).hasValue(1);
        assertThat(Stream.of(null, null, 1, null).collect(EasyIterables.getOnlyItemOrEmpty())).hasValue(1);
    }

    @Test
    public void getOnlyItemOrEmpty_nulls_and_several_values() {
        assertThat(Stream.of(1, 2, null).collect(EasyIterables.getOnlyItemOrEmpty())).isEmpty();
        assertThat(Stream.of(1, null, 2).collect(EasyIterables.getOnlyItemOrEmpty())).isEmpty();
        assertThat(Stream.of(1, null, null, 2).collect(EasyIterables.getOnlyItemOrEmpty())).isEmpty();
        assertThat(Stream.of(null, 1, null, 2).collect(EasyIterables.getOnlyItemOrEmpty())).isEmpty();
        assertThat(Stream.of(null, null, 1, 2, null).collect(EasyIterables.getOnlyItemOrEmpty())).isEmpty();
    }
}
