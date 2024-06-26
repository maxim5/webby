package io.spbx.util.collect;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ListBuilderTest {
    @Test
    public void builder_empty() {
        ListBuilder<Integer> builder = ListBuilder.builder();
        assertBuilder(builder);
    }

    @Test
    public void builder_reserve_empty() {
        ListBuilder<Integer> builder = ListBuilder.builder(3);
        assertBuilder(builder);
    }

    @Test
    public void builder_add_one_not_null() {
        ListBuilder<Integer> builder = ListBuilder.<Integer>builder().add(1);
        assertBuilder(builder, 1);
    }

    @Test
    public void builder_add_one_null() {
        ListBuilder<Integer> builder = ListBuilder.<Integer>builder().add(null);
        assertBuilder(builder, (Integer) null);
    }

    @Test
    public void builder_add_two_not_nulls() {
        ListBuilder<Integer> builder = ListBuilder.<Integer>builder().add(1).add(2);
        assertBuilder(builder, 1, 2);
    }

    @Test
    public void builder_add_two_null_and_not_null() {
        ListBuilder<Integer> builder = ListBuilder.<Integer>builder().add(1).add(null);
        assertBuilder(builder, 1, null);
    }

    @Test
    public void builder_addAll_one_not_null() {
        ListBuilder<Integer> builder = ListBuilder.<Integer>builder().addAll(1);
        assertBuilder(builder, 1);
    }

    @Test
    public void builder_addAll_one_null() {
        ListBuilder<Integer> builder = ListBuilder.<Integer>builder().addAll((Integer) null);
        assertBuilder(builder, (Integer) null);
    }

    @Test
    public void builder_addAll_two_not_nulls() {
        ListBuilder<Integer> builder = ListBuilder.<Integer>builder().addAll(1, 2);
        assertBuilder(builder, 1, 2);
    }

    @Test
    public void builder_addAll_two_nulls() {
        ListBuilder<Integer> builder = ListBuilder.<Integer>builder().addAll(null, null);
        assertBuilder(builder, null, null);
    }

    @Test
    public void builder_addAll_vararg_not_nulls() {
        ListBuilder<Integer> builder = ListBuilder.<Integer>builder().addAll(1, 2, 3, 4);
        assertBuilder(builder, 1, 2, 3, 4);
    }

    @Test
    public void builder_addAll_vararg_null_and_not_nulls() {
        ListBuilder<Integer> builder = ListBuilder.<Integer>builder().addAll(1, null, 2);
        assertBuilder(builder, 1, null, 2);
    }

    @Test
    public void builder_addAll_collection_not_nulls() {
        ListBuilder<Integer> builder = ListBuilder.<Integer>builder().addAll(List.of(1, 2, 3, 4));
        assertBuilder(builder, 1, 2, 3, 4);
    }

    @Test
    public void builder_addAll_collection_null_and_not_null() {
        ListBuilder<Integer> builder = ListBuilder.<Integer>builder().addAll(Arrays.asList(null, 1, null));
        assertBuilder(builder, null, 1, null);
    }

    @Test
    public void builder_addAll_iterable_not_nulls() {
        ListBuilder<Integer> builder = ListBuilder.<Integer>builder().addAll((Iterable<Integer>) List.of(1, 2, 3, 4));
        assertBuilder(builder, 1, 2, 3, 4);
    }

    @Test
    public void builder_addAll_iterable_null_and_not_null() {
        ListBuilder<Integer> builder = ListBuilder.<Integer>builder().addAll((Iterable<Integer>) Arrays.asList(null, 1));
        assertBuilder(builder, null, 1);
    }

    @Test
    public void builder_of_varargs() {
        assertBuilder(ListBuilder.<Integer>of());
        assertBuilder(ListBuilder.of(1), 1);
        assertBuilder(ListBuilder.of(1, 2), 1, 2);
        assertBuilder(ListBuilder.of(1, 2, null), 1, 2, null);
        assertBuilder(ListBuilder.of(null, null, null, 0), null, null, null, 0);
    }

    @Test
    public void builder_of_list() {
        assertBuilder(ListBuilder.of(List.<Integer>of()));
        assertBuilder(ListBuilder.of(List.of(1)), 1);
        assertBuilder(ListBuilder.of(List.of(1, 2)), 1, 2);
    }

    @Test
    public void builder_map() {
        assertBuilder(ListBuilder.<Long>of().map(Long::intValue));
        assertBuilder(ListBuilder.of(1L).map(Long::intValue), 1);
        assertBuilder(ListBuilder.of(1L, 2L).map(Long::intValue), 1, 2);
        assertThrows(NullPointerException.class, () -> ListBuilder.of(1L, 2L, null).map(Long::intValue));
    }

    @Test
    public void builder_mapSafe() {
        assertBuilder(ListBuilder.<Long>of().mapSafe(Long::intValue));
        assertBuilder(ListBuilder.of(1L).mapSafe(Long::intValue), 1);
        assertBuilder(ListBuilder.of(1L, 2L).mapSafe(Long::intValue), 1, 2);
        assertBuilder(ListBuilder.of(1L, 2L, null).mapSafe(Long::intValue), 1, 2, null);
        assertBuilder(ListBuilder.of(null, null, null, 0L).mapSafe(Long::intValue), null, null, null, 0);
    }

    @Test
    public void builder_excludeIf() {
        assertBuilder(ListBuilder.<Integer>of().excludeIf(x -> x < 0));
        assertBuilder(ListBuilder.of(0).excludeIf(x -> x < 0), 0);
        assertBuilder(ListBuilder.of(1).excludeIf(x -> x < 0), 1);
        assertBuilder(ListBuilder.of(1, 0, -1, 2).excludeIf(x -> x < 0), 1, 0, 2);
        assertBuilder(ListBuilder.of(-1, -2).excludeIf(x -> x < 0));
        assertThrows(NullPointerException.class, () -> ListBuilder.of(1, null).excludeIf(x -> x < 0));
    }

    @Test
    public void builder_excludeIfSafe() {
        assertBuilder(ListBuilder.<Integer>of().excludeIfSafe(x -> x < 0));
        assertBuilder(ListBuilder.of(0).excludeIfSafe(x -> x < 0), 0);
        assertBuilder(ListBuilder.of(1).excludeIfSafe(x -> x < 0), 1);
        assertBuilder(ListBuilder.of(1, null).excludeIfSafe(x -> x < 0), 1, null);
        assertBuilder(ListBuilder.of(1, 0, -1, 2).excludeIfSafe(x -> x < 0), 1, 0, 2);
        assertBuilder(ListBuilder.of(-1, -2).excludeIfSafe(x -> x < 0));
        assertBuilder(ListBuilder.of(null, null, -1, -2).excludeIfSafe(x -> x < 0), null, null);
    }

    @Test
    public void builder_of_withoutNulls() {
        assertBuilder(ListBuilder.<Integer>of().withoutNulls());
        assertBuilder(ListBuilder.of(1).withoutNulls(), 1);
        assertBuilder(ListBuilder.of(1, 2).withoutNulls(), 1, 2);
        assertBuilder(ListBuilder.of(1, 2, null).withoutNulls(), 1, 2);
        assertBuilder(ListBuilder.of(null, null, null, 0).withoutNulls(), 0);
        assertBuilder(ListBuilder.of((Integer) null).withoutNulls());
    }

    @Test
    public void builder_combine() {
        ListBuilder<Integer> builder = ListBuilder.<Integer>builder().addAll(1, 2)
            .combine(ListBuilder.<Integer>builder().addAll(3));
        assertBuilder(builder, 1, 2, 3);
    }

    @Test
    public void builder_concat_lists_not_nulls() {
        assertThat(ListBuilder.concat(List.of(1, 2), List.of(3))).containsExactly(1, 2, 3).inOrder();
    }

    @Test
    public void builder_concat_lists_nulls_and_not_nulls() {
        assertThat(ListBuilder.concat(Arrays.asList(1, null), Arrays.asList(null, 2)))
            .containsExactly(1, null, null, 2).inOrder();
    }

    @Test
    public void builder_concat_lists_nulls() {
        assertThat(ListBuilder.concat(Arrays.asList((Integer) null), Arrays.asList(null, null)))
            .containsExactly(null, null, null).inOrder();
    }

    @Test
    public void builder_concat_iterables_not_nulls() {
        assertThat(ListBuilder.concat((Iterable<Integer>) List.of(1, 2), List.of(3)))
            .containsExactly(1, 2, 3).inOrder();
    }

    @Test
    public void builder_concat_iterables_nulls_and_not_nulls() {
        assertThat(ListBuilder.concat((Iterable<Integer>) Arrays.asList(1, null), Arrays.asList(null, 2)))
            .containsExactly(1, null, null, 2).inOrder();
    }

    @Test
    public void builder_concat_iterables_nulls() {
        assertThat(ListBuilder.concat((Iterable<Integer>) Arrays.asList((Integer) null), Arrays.asList(null, null)))
            .containsExactly(null, null, null).inOrder();
    }

    @Test
    public void builder_concat_one_not_nulls() {
        assertThat(ListBuilder.concatOne(List.of(1, 2), 3)).containsExactly(1, 2, 3).inOrder();
    }

    @Test
    public void builder_concat_one_nulls_and_not_nulls() {
        assertThat(ListBuilder.concatOne(Arrays.asList(1, null), 2)).containsExactly(1, null, 2).inOrder();
    }

    @Test
    public void builder_concat_one_nulls() {
        assertThat(ListBuilder.concatOne(Arrays.asList(null, null), null)).containsExactly(null, null, null).inOrder();
    }

    @SafeVarargs
    private static <T> void assertBuilder(@NotNull ListBuilder<T> builder, @Nullable T @NotNull ... expected) {
        boolean hasNulls = Arrays.stream(expected).anyMatch(Objects::isNull);
        assertThat(builder.containsNulls()).isEqualTo(hasNulls);

        if (hasNulls) {
            assertThrows(AssertionError.class, builder::toList);
            assertThrows(AssertionError.class, builder::toGuavaImmutableList);
        } else {
            assertThat(builder.toList()).containsExactlyElementsIn(expected).inOrder();
            assertThat(builder.toGuavaImmutableList()).containsExactlyElementsIn(expected).inOrder();
        }

        assertThat(builder.toArray()).containsExactlyElementsIn(expected).inOrder();
        assertThat(builder.toArrayList()).containsExactlyElementsIn(expected).inOrder();
        assertThat(builder.toImmutableArray()).containsExactlyElementsIn(expected).inOrder();
        assertThat(builder.toImmutableArrayList()).containsExactlyElementsIn(expected).inOrder();
        // FIX[debt]: A quick hack. Refactor to subjects.
        assertThat(builder.toNativeArray(value -> (T[]) new Integer[value])).asList().containsExactly(expected);
    }
}
