package io.webby.util.collect;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.util.collect.ImmutableArrayList.toImmutableArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ImmutableArrayListTest {
    @Test
    public void arrayList_of_simple_not_null_items() {
        assertArrayList(ImmutableArrayList.of());
        assertArrayList(ImmutableArrayList.of(1), 1);
        assertArrayList(ImmutableArrayList.of(1, 2), 1, 2);
        assertArrayList(ImmutableArrayList.of(1, 2, 3), 1, 2, 3);
    }

    @Test
    public void arrayList_of_simple_null_items() {
        assertArrayList(ImmutableArrayList.of(null), (Object) null);
        assertArrayList(ImmutableArrayList.of(1, null), 1, null);
        assertArrayList(ImmutableArrayList.of(null, null), null, null);
    }

    @Test
    public void copyOf_simple() {
        assertArrayList(ImmutableArrayList.copyOf(1, 2, 3, 4), 1, 2, 3, 4);
        assertArrayList(ImmutableArrayList.copyOf(List.of(1, 2, 3, 4)), 1, 2, 3, 4);
        assertArrayList(ImmutableArrayList.copyOf((Iterable<Integer>) List.of(1, 2, 3, 4)), 1, 2, 3, 4);
    }

    @Test
    public void subList_simple() {
        assertArrayList(ImmutableArrayList.copyOf(1, 2, 3).subList(0, 0));
        assertArrayList(ImmutableArrayList.copyOf(1, 2, 3).subList(0, 1), 1);
        assertArrayList(ImmutableArrayList.copyOf(1, 2, 3).subList(1, 1));
        assertArrayList(ImmutableArrayList.copyOf(1, 2, 3).subList(1, 2), 2);
        assertArrayList(ImmutableArrayList.copyOf(1, 2, 3).subList(0, 2), 1, 2);
        assertArrayList(ImmutableArrayList.copyOf(1, 2, 3).subList(2, 3), 3);
        assertArrayList(ImmutableArrayList.copyOf(1, 2, 3).subList(3, 3));
        assertArrayList(ImmutableArrayList.copyOf(1, 2, 3).subList(0, 3), 1, 2, 3);
    }

    @Test
    public void toImmutableArrayList_simple() {
        assertEquals(Stream.of().collect(toImmutableArrayList()), ImmutableArrayList.of());
        assertEquals(Stream.of(1).collect(toImmutableArrayList()), ImmutableArrayList.of(1));
        assertEquals(Stream.of(1, 2).collect(toImmutableArrayList()), ImmutableArrayList.of(1, 2));
        assertEquals(Stream.of(1, 2, 3).collect(toImmutableArrayList()), ImmutableArrayList.of(1, 2, 3));
    }

    @Test
    public void toImmutableArrayList_nulls() {
        assertEquals(Stream.of((Object) null).collect(toImmutableArrayList()), ImmutableArrayList.of(null));
        assertEquals(Stream.of(1, null).collect(toImmutableArrayList()), ImmutableArrayList.of(1, null));
        assertEquals(Stream.of(null, null).collect(toImmutableArrayList()), ImmutableArrayList.of(null, null));
        assertEquals(Stream.of(null, 1, null).collect(toImmutableArrayList()), ImmutableArrayList.of(null, 1, null));
    }

    @SafeVarargs
    private static <T> void assertArrayList(@NotNull ImmutableArrayList<T> list, @Nullable T @NotNull ... expected) {
        assertThat(list).hasSize(expected.length);
        assertThat(list).containsExactlyElementsIn(expected).inOrder();
        assertThat(Lists.newArrayList(list.iterator())).containsExactlyElementsIn(expected).inOrder();
        assertThat(Lists.newArrayList(list.listIterator())).containsExactlyElementsIn(expected).inOrder();
        assertThat(list.isEmpty()).isEqualTo(expected.length == 0);

        List<T> expectedList = Arrays.asList(expected);
        for (int i = 0; i < expected.length; i++) {
            T item = expected[i];
            assertEquals(item, list.get(i));
            assertEquals(expectedList.indexOf(item), list.indexOf(item));
            assertEquals(expectedList.lastIndexOf(item), list.lastIndexOf(item));
            assertTrue(list.contains(item));
            assertThat(list.subList(i, i + 1)).containsExactly(item);
        }
        assertThat(list.subList(0, expected.length)).containsExactlyElementsIn(expected).inOrder();
    }

}
