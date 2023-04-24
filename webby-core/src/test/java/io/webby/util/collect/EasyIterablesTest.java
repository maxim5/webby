package io.webby.util.collect;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.testing.TestingBasics.iterable;
import static io.webby.testing.TestingBasics.sortedSetOf;
import static org.junit.jupiter.api.Assertions.*;

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
        assertEquals(EasyIterables.estimateSize(List.of(), -1), 0);
        assertEquals(EasyIterables.estimateSize(List.of(1), -1), 1);
        assertEquals(EasyIterables.estimateSize(List.of(1, 2), -1), 2);
    }

    @Test
    public void allEqual() {
        assertTrue(EasyIterables.allEqual(Stream.of()));
        assertTrue(EasyIterables.allEqual(Stream.of(1)));
        assertTrue(EasyIterables.allEqual(Stream.of(1, 1)));
        assertTrue(EasyIterables.allEqual(Stream.of(1, 1, 1)));
        assertFalse(EasyIterables.allEqual(Stream.of(1, 2)));
        assertFalse(EasyIterables.allEqual(Stream.of(1, 2, 1)));
        assertFalse(EasyIterables.allEqual(Stream.of(1, 2, 2)));
        assertFalse(EasyIterables.allEqual(Stream.of(1, 1, 2)));
    }

    @Test
    public void getOnlyItemOrEmpty_simple() {
        assertEquals(Stream.of().collect(EasyIterables.getOnlyItemOrEmpty()), Optional.empty());
        assertEquals(Stream.of(1).collect(EasyIterables.getOnlyItemOrEmpty()), Optional.of(1));
        assertEquals(Stream.of(1, 2).collect(EasyIterables.getOnlyItemOrEmpty()), Optional.empty());
        assertEquals(Stream.of(1, 2, 3).collect(EasyIterables.getOnlyItemOrEmpty()), Optional.empty());
        assertEquals(Stream.of(1, 2, 3, 4).collect(EasyIterables.getOnlyItemOrEmpty()), Optional.empty());
    }

    @Test
    public void getOnlyItemOrEmpty_all_nulls() {
        assertEquals(Stream.of((Object) null).collect(EasyIterables.getOnlyItemOrEmpty()), Optional.empty());
        assertEquals(Stream.of(null, null).collect(EasyIterables.getOnlyItemOrEmpty()), Optional.empty());
        assertEquals(Stream.of(null, null, null).collect(EasyIterables.getOnlyItemOrEmpty()), Optional.empty());
    }

    @Test
    public void getOnlyItemOrEmpty_nulls_and_one_value() {
        assertEquals(Stream.of(1, null).collect(EasyIterables.getOnlyItemOrEmpty()), Optional.of(1));
        assertEquals(Stream.of(null, 1).collect(EasyIterables.getOnlyItemOrEmpty()), Optional.of(1));
        assertEquals(Stream.of(null, null, 1, null).collect(EasyIterables.getOnlyItemOrEmpty()), Optional.of(1));
    }

    @Test
    public void getOnlyItemOrEmpty_nulls_and_several_values() {
        assertEquals(Stream.of(1, 2, null).collect(EasyIterables.getOnlyItemOrEmpty()), Optional.empty());
        assertEquals(Stream.of(1, null, 2).collect(EasyIterables.getOnlyItemOrEmpty()), Optional.empty());
        assertEquals(Stream.of(1, null, null, 2).collect(EasyIterables.getOnlyItemOrEmpty()), Optional.empty());
        assertEquals(Stream.of(null, 1, null, 2).collect(EasyIterables.getOnlyItemOrEmpty()), Optional.empty());
        assertEquals(Stream.of(null, null, 1, 2, null).collect(EasyIterables.getOnlyItemOrEmpty()), Optional.empty());
    }
}
