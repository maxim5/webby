package io.spbx.util.collect;

import com.google.common.collect.Lists;
import com.google.errorprone.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static io.spbx.util.collect.ImmutableArrayList.toImmutableArrayList;

public class ImmutableArrayListTest {
    @Test
    public void arrayList_of_simple_not_null_items() {
        assertArrayList(ImmutableArrayList.of()).isEmpty();
        assertArrayList(ImmutableArrayList.of(1)).containsExactly(1);
        assertArrayList(ImmutableArrayList.of(1, 2)).containsExactly(1, 2);
        assertArrayList(ImmutableArrayList.of(1, 2, 3)).containsExactly(1, 2, 3);
    }

    @Test
    public void arrayList_of_simple_null_items() {
        assertArrayList(ImmutableArrayList.of(null)).containsExactly((Object) null);
        assertArrayList(ImmutableArrayList.of(1, null)).containsExactly(1, null);
        assertArrayList(ImmutableArrayList.of(null, null)).containsExactly(null, null);
    }

    @Test
    public void copyOf_simple() {
        assertArrayList(ImmutableArrayList.copyOf(1, 2, 3, 4)).containsExactly(1, 2, 3, 4);
        assertArrayList(ImmutableArrayList.copyOf(List.of(1, 2, 3, 4))).containsExactly(1, 2, 3, 4);
        assertArrayList(ImmutableArrayList.copyOf((Iterable<Integer>) List.of(1, 2, 3, 4))).containsExactly(1, 2, 3, 4);
    }

    @Test
    public void subList_simple() {
        assertArrayList(ImmutableArrayList.copyOf(1, 2, 3).subList(0, 0)).isEmpty();
        assertArrayList(ImmutableArrayList.copyOf(1, 2, 3).subList(0, 1)).containsExactly(1);
        assertArrayList(ImmutableArrayList.copyOf(1, 2, 3).subList(1, 1)).isEmpty();
        assertArrayList(ImmutableArrayList.copyOf(1, 2, 3).subList(1, 2)).containsExactly(2);
        assertArrayList(ImmutableArrayList.copyOf(1, 2, 3).subList(0, 2)).containsExactly(1, 2);
        assertArrayList(ImmutableArrayList.copyOf(1, 2, 3).subList(2, 3)).containsExactly(3);
        assertArrayList(ImmutableArrayList.copyOf(1, 2, 3).subList(3, 3)).isEmpty();
        assertArrayList(ImmutableArrayList.copyOf(1, 2, 3).subList(0, 3)).containsExactly(1, 2, 3);
    }

    @Test
    public void toImmutableArrayList_simple() {
        assertThat(Stream.of().collect(toImmutableArrayList())).isEqualTo(ImmutableArrayList.of());
        assertThat(Stream.of(1).collect(toImmutableArrayList())).isEqualTo(ImmutableArrayList.of(1));
        assertThat(Stream.of(1, 2).collect(toImmutableArrayList())).isEqualTo(ImmutableArrayList.of(1, 2));
        assertThat(Stream.of(1, 2, 3).collect(toImmutableArrayList())).isEqualTo(ImmutableArrayList.of(1, 2, 3));
    }

    @Test
    public void toImmutableArrayList_nulls() {
        assertThat(Stream.of((Object) null).collect(toImmutableArrayList())).isEqualTo(ImmutableArrayList.of(null));
        assertThat(Stream.of(1, null).collect(toImmutableArrayList())).isEqualTo(ImmutableArrayList.of(1, null));
        assertThat(Stream.of(null, null).collect(toImmutableArrayList())).isEqualTo(ImmutableArrayList.of(null, null));
        assertThat(Stream.of(null, 1, null).collect(toImmutableArrayList())).isEqualTo(ImmutableArrayList.of(null, 1, null));
    }

    @CheckReturnValue
    private static <T> @NotNull ImmutableArrayListSubject<T> assertArrayList(@NotNull ImmutableArrayList<T> list) {
        return new ImmutableArrayListSubject<>(list);
    }

    private record ImmutableArrayListSubject<T>(@NotNull ImmutableArrayList<T> list) {
        public void isEmpty() {
            containsExactly();
        }

        @SafeVarargs
        public final void containsExactly(@Nullable T @NotNull ... expected) {
            assertThat(list).hasSize(expected.length);
            assertThat(list).containsExactlyElementsIn(expected).inOrder();
            assertThat(Lists.newArrayList(list.iterator())).containsExactlyElementsIn(expected).inOrder();
            assertThat(Lists.newArrayList(list.listIterator())).containsExactlyElementsIn(expected).inOrder();
            assertThat(list.isEmpty()).isEqualTo(expected.length == 0);

            List<T> expectedList = Arrays.asList(expected);
            for (int i = 0; i < expected.length; i++) {
                T item = expected[i];
                assertThat(list.get(i)).isEqualTo(item);
                assertThat(list.indexOf(item)).isEqualTo(expectedList.indexOf(item));
                assertThat(list.lastIndexOf(item)).isEqualTo(expectedList.lastIndexOf(item));
                assertThat(list.contains(item)).isTrue();
                assertThat(list.subList(i, i + 1)).containsExactly(item);
            }
            assertThat(list.subList(0, expected.length)).containsExactlyElementsIn(expected).inOrder();
        }
    }
}
