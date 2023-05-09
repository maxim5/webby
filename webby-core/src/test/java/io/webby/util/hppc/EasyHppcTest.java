package io.webby.util.hppc;

import com.carrotsearch.hppc.*;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.carrotsearch.hppc.cursors.LongCursor;
import com.google.common.truth.IterableSubject;
import io.webby.testing.MockConsumer;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.testing.AssertPrimitives.*;
import static io.webby.testing.TestingBasics.array;
import static io.webby.testing.TestingPrimitives.*;
import static org.junit.jupiter.api.Assertions.*;

public class EasyHppcTest {
    @Test
    public void int_list_slice() {
        IntArrayList list = IntArrayList.from(1, 2, 3);

        assertIntsOrdered(EasyHppc.slice(list, 0, 2), 1, 2);
        assertIntsOrdered(EasyHppc.slice(list, 0, 3), 1, 2, 3);
        assertIntsOrdered(EasyHppc.slice(list, 0, 4), 1, 2, 3);
        assertIntsOrdered(EasyHppc.slice(list, 1, 2), 2);
        assertIntsOrdered(EasyHppc.slice(list, 1, 1));
    }

    @Test
    public void long_list_slice() {
        LongArrayList list = LongArrayList.from(1, 2, 3);

        assertLongsOrdered(EasyHppc.slice(list, 0, 2), 1, 2);
        assertLongsOrdered(EasyHppc.slice(list, 0, 3), 1, 2, 3);
        assertLongsOrdered(EasyHppc.slice(list, 0, 4), 1, 2, 3);
        assertLongsOrdered(EasyHppc.slice(list, 1, 2), 2);
        assertLongsOrdered(EasyHppc.slice(list, 1, 1));
    }

    @Test
    public void int_iterate_chunks() {
        try (MockConsumer.Tracker ignored = MockConsumer.trackAllConsumersDone()) {
            EasyHppc.iterateChunks(IntArrayList.from(), 2, MockConsumer.expecting());
            EasyHppc.iterateChunks(IntArrayList.from(1), 2, MockConsumer.expecting(IntArrayList.from(1)));
            EasyHppc.iterateChunks(IntArrayList.from(1, 2), 2, MockConsumer.expecting(IntArrayList.from(1, 2)));
            EasyHppc.iterateChunks(IntArrayList.from(1, 2, 3), 2,
                                   MockConsumer.expecting(IntArrayList.from(1, 2), IntArrayList.from(3)));
            EasyHppc.iterateChunks(IntArrayList.from(1, 2, 3, 4), 2,
                                   MockConsumer.expecting(IntArrayList.from(1, 2), IntArrayList.from(3, 4)));
        }
    }

    @Test
    public void long_iterate_chunks() {
        try (MockConsumer.Tracker ignored = MockConsumer.trackAllConsumersDone()) {
            EasyHppc.iterateChunks(LongArrayList.from(), 2, MockConsumer.expecting());
            EasyHppc.iterateChunks(LongArrayList.from(1), 2, MockConsumer.expecting(LongArrayList.from(1)));
            EasyHppc.iterateChunks(LongArrayList.from(1, 2), 2, MockConsumer.expecting(LongArrayList.from(1, 2)));
            EasyHppc.iterateChunks(LongArrayList.from(1, 2, 3), 2,
                                   MockConsumer.expecting(LongArrayList.from(1, 2), LongArrayList.from(3)));
            EasyHppc.iterateChunks(LongArrayList.from(1, 2, 3, 4), 2,
                                   MockConsumer.expecting(LongArrayList.from(1, 2), LongArrayList.from(3, 4)));
        }
    }

    @Test
    public void int_map_slice() {
        IntIntHashMap map = IntIntHashMap.from(ints(1, 2), ints(7, 8));

        assertInts(EasyHppc.slice(map, ints(1)), 1, 7);
        assertInts(EasyHppc.slice(map, IntArrayList.from(1)), 1, 7);

        assertInts(EasyHppc.slice(map, ints(2)), 2, 8);
        assertInts(EasyHppc.slice(map, IntArrayList.from(2)), 2, 8);

        assertInts(EasyHppc.slice(map, ints(1, 2)), 1, 7, 2, 8);
        assertInts(EasyHppc.slice(map, IntArrayList.from(1, 2)), 1, 7, 2, 8);

        assertInts(EasyHppc.slice(map, ints(1, 3)), 1, 7, 3, 0);
        assertInts(EasyHppc.slice(map, IntArrayList.from(1, 3)), 1, 7, 3, 0);

        assertInts(EasyHppc.slice(map, ints(3)), 3, 0);
        assertInts(EasyHppc.slice(map, IntArrayList.from(3)), 3, 0);

        assertInts(EasyHppc.slice(map, ints()));
        assertInts(EasyHppc.slice(map, IntArrayList.from()));
    }

    @Test
    public void int_to_array_list() {
        assertIntsOrdered(EasyHppc.toArrayList(IntArrayList.from(1, 2, 3)), 1, 2, 3);
        assertIntsNoOrder(EasyHppc.toArrayList(IntHashSet.from(1, 2, 3)), 1, 2, 3);
    }

    @Test
    public void long_to_array_list() {
        assertLongsOrdered(EasyHppc.toArrayList(LongArrayList.from(1, 2, 3)), 1, 2, 3);
        assertLongsNoOrder(EasyHppc.toArrayList(LongHashSet.from(1, 2, 3)), 1, 2, 3);
    }

    @Test
    public void int_from_java_iterable() {
        assertIntsOrdered(EasyHppc.fromJavaIterableInt(List.of()));
        assertIntsOrdered(EasyHppc.fromJavaIterableInt(List.of(3, 2, 1)), 3, 2, 1);
    }

    @Test
    public void long_from_java_iterable() {
        assertLongsOrdered(EasyHppc.fromJavaIterableLong(List.of()));
        assertLongsOrdered(EasyHppc.fromJavaIterableLong(List.of(3L, 2L, 1L)), 3, 2, 1);
    }

    @Test
    public void int_list_to_java_stream() {
        assertIntStreamThat(EasyHppc.toJavaStream(IntArrayList.from())).isEmpty();
        assertIntStreamThat(EasyHppc.toJavaStream(IntArrayList.from(1))).containsExactly(1).inOrder();
        assertIntStreamThat(EasyHppc.toJavaStream(IntArrayList.from(1, 2, 3))).containsExactly(1, 2, 3).inOrder();
    }

    @Test
    public void long_list_to_java_stream() {
        assertLongStreamThat(EasyHppc.toJavaStream(LongArrayList.from())).isEmpty();
        assertLongStreamThat(EasyHppc.toJavaStream(LongArrayList.from(1L))).containsExactly(1L).inOrder();
        assertLongStreamThat(EasyHppc.toJavaStream(LongArrayList.from(1L, 2L, 3L))).containsExactly(1L, 2L, 3L).inOrder();
    }

    @Test
    public void int_list_to_java_list() {
        assertThat(EasyHppc.toJavaList(IntArrayList.from())).isEmpty();
        assertThat(EasyHppc.toJavaList(IntArrayList.from(1))).containsExactly(1).inOrder();
        assertThat(EasyHppc.toJavaList(IntArrayList.from(1, 2, 3))).containsExactly(1, 2, 3).inOrder();
    }

    @Test
    public void long_list_to_java_list() {
        assertThat(EasyHppc.toJavaList(LongArrayList.from())).isEmpty();
        assertThat(EasyHppc.toJavaList(LongArrayList.from(1L))).containsExactly(1L).inOrder();
        assertThat(EasyHppc.toJavaList(LongArrayList.from(1L, 2L, 3L))).containsExactly(1L, 2L, 3L).inOrder();
    }

    @Test
    public void int_int_map_to_java_map() {
        assertThat(EasyHppc.toJavaMap(new IntIntHashMap())).isEmpty();
        assertThat(EasyHppc.toJavaMap(IntIntHashMap.from(ints(1), ints(7)))).containsExactly(1, 7);
        assertThat(EasyHppc.toJavaMap(IntIntHashMap.from(ints(1, 2), ints(7, 8)))).containsExactly(1, 7, 2, 8);
    }

    @Test
    public void int_obj_map_to_java_map() {
        assertThat(EasyHppc.toJavaMap(new IntObjectHashMap<>())).isEmpty();
        assertThat(EasyHppc.toJavaMap(IntObjectHashMap.from(ints(1), array("foo")))).containsExactly(1, "foo");
        assertThat(EasyHppc.toJavaMap(IntObjectHashMap.from(ints(1, 2), array("a", "b")))).containsExactly(1, "a", 2, "b");
    }

    @Test
    public void collect_from_int_stream() {
        assertIntsOrdered(EasyHppc.collectFromIntStream(IntStream.of()));
        assertIntsOrdered(EasyHppc.collectFromIntStream(IntStream.of(1)), 1);
        assertIntsOrdered(EasyHppc.collectFromIntStream(IntStream.of(1, 2)), 1, 2);
    }

    @Test
    public void int_union() {
        assertIntsNoOrder(EasyHppc.union(IntHashSet.from(), IntHashSet.from()));
        assertIntsNoOrder(EasyHppc.union(IntHashSet.from(1, 2, 3), IntHashSet.from()), 1, 2, 3);
        assertIntsNoOrder(EasyHppc.union(IntHashSet.from(), IntHashSet.from(1, 2, 3)), 1, 2, 3);
        assertIntsNoOrder(EasyHppc.union(IntHashSet.from(1), IntHashSet.from(2)), 1, 2);
        assertIntsNoOrder(EasyHppc.union(IntHashSet.from(1, 2), IntHashSet.from(1, 2)), 1, 2);
        assertIntsNoOrder(EasyHppc.union(IntHashSet.from(1, 2, 3), IntHashSet.from(3, 4)), 1, 2, 3, 4);
        assertIntsNoOrder(EasyHppc.union(IntHashSet.from(1, 2, 3), IntHashSet.from(4, 5)), 1, 2, 3, 4, 5);
    }

    @Test
    public void int_intersect() {
        assertIntsNoOrder(EasyHppc.intersect(IntHashSet.from(), IntHashSet.from()));
        assertIntsNoOrder(EasyHppc.intersect(IntHashSet.from(1, 2, 3), IntHashSet.from()));
        assertIntsNoOrder(EasyHppc.intersect(IntHashSet.from(), IntHashSet.from(1, 2, 3)));
        assertIntsNoOrder(EasyHppc.intersect(IntHashSet.from(1), IntHashSet.from(2)));
        assertIntsNoOrder(EasyHppc.intersect(IntHashSet.from(1, 2), IntHashSet.from(1, 2)), 1, 2);
        assertIntsNoOrder(EasyHppc.intersect(IntHashSet.from(1, 2, 3), IntHashSet.from(3, 4)), 3);
        assertIntsNoOrder(EasyHppc.intersect(IntHashSet.from(1, 2, 3), IntHashSet.from(4, 5)));
    }

    @Test
    public void int_subtract() {
        assertIntsNoOrder(EasyHppc.subtract(IntHashSet.from(), IntHashSet.from()));
        assertIntsNoOrder(EasyHppc.subtract(IntHashSet.from(1, 2, 3), IntHashSet.from()), 1, 2, 3);
        assertIntsNoOrder(EasyHppc.subtract(IntHashSet.from(), IntHashSet.from(1, 2, 3)));
        assertIntsNoOrder(EasyHppc.subtract(IntHashSet.from(1), IntHashSet.from(2)), 1);
        assertIntsNoOrder(EasyHppc.subtract(IntHashSet.from(1, 2), IntHashSet.from(1, 2)));
        assertIntsNoOrder(EasyHppc.subtract(IntHashSet.from(1, 2, 3), IntHashSet.from(3, 4)), 1, 2);
        assertIntsNoOrder(EasyHppc.subtract(IntHashSet.from(1, 2, 3), IntHashSet.from(4, 5)), 1, 2, 3);
    }

    @Test
    public void int_retain_all_copy() {
        assertIntsNoOrder(EasyHppc.retainAllCopy(IntHashSet.from(), x -> x % 2 == 0));
        assertIntsNoOrder(EasyHppc.retainAllCopy(IntHashSet.from(1), x -> x % 2 == 0));
        assertIntsNoOrder(EasyHppc.retainAllCopy(IntHashSet.from(2), x -> x % 2 == 0), 2);
        assertIntsNoOrder(EasyHppc.retainAllCopy(IntHashSet.from(1, 3), x -> x % 2 == 0));
        assertIntsNoOrder(EasyHppc.retainAllCopy(IntHashSet.from(1, 2, 3), x -> x % 2 == 0), 2);
    }

    @Test
    public void int_remove_all_copy() {
        assertIntsNoOrder(EasyHppc.removeAllCopy(IntHashSet.from(), x -> x % 2 == 0));
        assertIntsNoOrder(EasyHppc.removeAllCopy(IntHashSet.from(1), x -> x % 2 == 0), 1);
        assertIntsNoOrder(EasyHppc.removeAllCopy(IntHashSet.from(2), x -> x % 2 == 0));
        assertIntsNoOrder(EasyHppc.removeAllCopy(IntHashSet.from(1, 3), x -> x % 2 == 0), 1, 3);
        assertIntsNoOrder(EasyHppc.removeAllCopy(IntHashSet.from(1, 2, 3), x -> x % 2 == 0), 1, 3);
    }

    @Test
    public void computeIfAbsent_int_int_exists() {
        IntIntMap map = newIntMap(1, 111, 2, 222);
        assertEquals(EasyHppc.computeIfAbsent(map, 1, () -> fail("Must not be called")), 111);
        assertInts(map, 1, 111, 2, 222);
    }

    @Test
    public void computeIfAbsent_int_int_exists_zero() {
        IntIntMap map = newIntMap(1, 0);
        assertEquals(EasyHppc.computeIfAbsent(map, 1, () -> fail("Must not be called")), 0);
        assertInts(map, 1, 0);
    }

    @Test
    public void computeIfAbsent_int_int_not_exists() {
        IntIntMap map = newIntMap(1, 111, 2, 222);
        assertEquals(EasyHppc.computeIfAbsent(map, 3, () -> 333), 333);
        assertInts(map, 1, 111, 2, 222, 3, 333);
    }

    @Test
    public void computeIfAbsent_int_obj_exists() {
        IntObjectMap<String> map = newIntObjectMap(1, "111", 2, "222");
        assertEquals(EasyHppc.computeIfAbsent(map, 1, () -> fail("Must not be called")), "111");
        assertMap(map, 1, "111", 2, "222");
    }

    @Test
    public void computeIfAbsent_int_obj_exists_null() {
        IntObjectMap<String> map = newIntObjectMap(1, null);
        assertNull(EasyHppc.computeIfAbsent(map, 1, () -> fail("Must not be called")));
        assertMap(map, 1, null);
    }

    @Test
    public void computeIfAbsent_int_obj_not_exists() {
        IntObjectMap<String> map = newIntObjectMap(1, "111", 2, "222");
        assertEquals(EasyHppc.computeIfAbsent(map, 3, () -> "333"), "333");
        assertMap(map, 1, "111", 2, "222", 3, "333");
    }

    private static @NotNull IterableSubject assertIntStreamThat(@NotNull Stream<IntCursor> stream) {
        return assertThat(stream.map(cursor -> cursor.value).toList());
    }

    private static @NotNull IterableSubject assertLongStreamThat(@NotNull Stream<LongCursor> stream) {
        return assertThat(stream.map(cursor -> cursor.value).toList());
    }
}
