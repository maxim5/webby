package io.webby.hppc;

import com.carrotsearch.hppc.*;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.google.common.truth.IterableSubject;
import io.webby.util.hppc.EasyHppc;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.testing.AssertPrimitives.*;
import static io.webby.testing.TestingPrimitives.ints;
import static io.webby.testing.TestingUtil.array;

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
    public void int_list_to_java_stream() {
        assertStreamThat(EasyHppc.toJavaStream(IntArrayList.from())).isEmpty();
        assertStreamThat(EasyHppc.toJavaStream(IntArrayList.from(1))).containsExactly(1).inOrder();
        assertStreamThat(EasyHppc.toJavaStream(IntArrayList.from(1, 2, 3))).containsExactly(1, 2, 3).inOrder();
    }

    @Test
    public void int_list_to_java_list() {
        assertThat(EasyHppc.toJavaList(IntArrayList.from())).isEmpty();
        assertThat(EasyHppc.toJavaList(IntArrayList.from(1))).containsExactly(1).inOrder();
        assertThat(EasyHppc.toJavaList(IntArrayList.from(1, 2, 3))).containsExactly(1, 2, 3).inOrder();
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

    private static @NotNull IterableSubject assertStreamThat(@NotNull Stream<IntCursor> stream) {
        return assertThat(stream.map(cursor -> cursor.value).toList());
    }
}
