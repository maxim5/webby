package io.webby.hppc;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntIntHashMap;
import com.carrotsearch.hppc.IntObjectHashMap;
import io.webby.util.hppc.EasyHppc;
import org.junit.Test;

import java.util.stream.IntStream;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.testing.AssertPrimitives.assertInts;
import static io.webby.testing.AssertPrimitives.assertIntsOrdered;
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
}
