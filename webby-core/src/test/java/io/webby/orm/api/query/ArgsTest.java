package io.webby.orm.api.query;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.LongArrayList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.mockrunner.mock.jdbc.MockPreparedStatement;
import io.webby.testing.TestingBasics;
import io.webby.util.collect.ImmutableArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.orm.testing.MockingJdbc.assertThat;
import static io.webby.orm.testing.MockingJdbc.mockPreparedStatement;
import static io.webby.testing.AssertBasics.assertPrivateFieldClass;
import static io.webby.testing.AssertBasics.getPrivateFieldValue;
import static io.webby.util.base.Unchecked.Suppliers.runRethrow;
import static org.junit.jupiter.api.Assertions.*;

public class ArgsTest {
    private static final Object NULL = null;
    private static final UnresolvedArg UNRESOLVED_A = new UnresolvedArg("a", 0);
    private static final UnresolvedArg UNRESOLVED_B = new UnresolvedArg("b", null);

    @Test
    public void of_not_null_objects() {
        with(Args.of())
            .assertAllResolved()
            .assertItems();

        with(Args.of("1"))
            .assertAllResolved()
            .assertItems("1");

        with(Args.of("1", "2", "3"))
            .assertAllResolved()
            .assertItems("1", "2", "3");

        with(Args.of(1, "2", 3.0))
            .assertAllResolved()
            .assertItems(1, "2", 3.0);
    }

    @Test
    public void of_nullable_objects() {
        with(Args.of(NULL))
            .assertAllResolved()
            .assertItems(NULL);

        with(Args.of(NULL, 1))
            .assertAllResolved()
            .assertItems(NULL, 1);

        with(Args.of(NULL, NULL, NULL))
            .assertAllResolved()
            .assertItems(NULL, NULL, NULL);
    }

    @Test
    public void of_ints() {
        with(Args.of(1))
            .assertAllResolved()
            .assertItems(1)
            .assertInternalType(IntArrayList.class);

        with(Args.of(1, 2))
            .assertAllResolved()
            .assertItems(1, 2)
            .assertInternalType(IntArrayList.class);

        with(Args.of(IntArrayList.from(1, 2, 3)))
            .assertAllResolved()
            .assertItems(1, 2, 3)
            .assertInternalType(IntArrayList.class);
    }

    @Test
    public void of_longs() {
        with(Args.of(1L))
            .assertAllResolved()
            .assertItems(1L)
            .assertInternalType(LongArrayList.class);

        with(Args.of(1L, 2L))
            .assertAllResolved()
            .assertItems(1L, 2L)
            .assertInternalType(LongArrayList.class);

        with(Args.of(LongArrayList.from(1, 2, 3)))
            .assertAllResolved()
            .assertItems(1L, 2L, 3L)
            .assertInternalType(LongArrayList.class);
    }

    @Test
    public void concat_not_null_objects() {
        with(Args.concat(Args.of(), Args.of("1")))
            .assertAllResolved()
            .assertItems("1");

        with(Args.concat(Args.of("1"), Args.of()))
            .assertAllResolved()
            .assertItems("1");

        with(Args.concat(Args.of("1"), Args.of("2", "3")))
            .assertAllResolved()
            .assertItems("1", "2", "3");
    }

    @Test
    public void concat_nullable_objects() {
        with(Args.concat(Args.of(), Args.of(NULL, 1)))
            .assertAllResolved()
            .assertItems(NULL, 1);

        with(Args.concat(Args.of(NULL), Args.of()))
            .assertAllResolved()
            .assertItems(NULL);

        with(Args.concat(Args.of(NULL), Args.of(NULL, NULL)))
            .assertAllResolved()
            .assertItems(NULL, NULL, NULL);
    }

    @Test
    public void concat_primitives() {
        with(Args.concat(Args.of(), Args.of(1)))
            .assertAllResolved()
            .assertItems(1);

        with(Args.concat(Args.of(1), Args.of()))
            .assertAllResolved()
            .assertItems(1);

        with(Args.concat(Args.of(1), Args.of(2, 3)))
            .assertAllResolved()
            .assertItems(1, 2, 3);

        with(Args.concat(Args.of(1), Args.of(2L, 3L)))
            .assertAllResolved()
            .assertItems(1, 2L, 3L);
    }

    @Test
    public void concat_primitives_with_nullable_objects() {
        with(Args.concat(Args.of(1), Args.of("1")))
            .assertAllResolved()
            .assertItems(1, "1");

        with(Args.concat(Args.of("1"), Args.of(1)))
            .assertAllResolved()
            .assertItems("1", 1);

        with(Args.concat(Args.of(1), Args.of(NULL, NULL)))
            .assertAllResolved()
            .assertItems(1, NULL, NULL);

        with(Args.concat(Args.of(1L), Args.of(NULL)))
            .assertAllResolved()
            .assertItems(1L, NULL);

        with(Args.concat(Args.of(NULL), Args.of(1L)))
            .assertAllResolved()
            .assertItems(NULL, 1L);
    }

    @Test
    public void flattenArgsOf_not_null() {
        with(Args.flattenArgsOf(wrap(Args.of()), wrap(Args.of(1))))
            .assertAllResolved()
            .assertItems(1);

        with(Args.flattenArgsOf(wrap(Args.of("0")), wrap(Args.of(1, 2))))
            .assertAllResolved()
            .assertItems("0", 1, 2);

        with(Args.flattenArgsOf(wrap(Args.of("0")), wrap(Args.of(1, 2)), wrap(Args.of(3L))))
            .assertAllResolved()
            .assertItems("0", 1, 2, 3L);

        with(Args.flattenArgsOf(wrap(Args.of("0")), wrap(Args.of(1, 2)), wrap(Args.of()), wrap(Args.of(3L))))
            .assertAllResolved()
            .assertItems("0", 1, 2, 3L);

        with(Args.flattenArgsOf(List.of(wrap(Args.of("0")), wrap(Args.of(1, 2)), wrap(Args.of()), wrap(Args.of(3L)))))
            .assertAllResolved()
            .assertItems("0", 1, 2, 3L);
    }

    @Test
    public void flattenArgsOf_nullable() {
        with(Args.flattenArgsOf(wrap(Args.of()), wrap(Args.of(NULL))))
            .assertAllResolved()
            .assertItems(NULL);

        with(Args.flattenArgsOf(wrap(Args.of("0")), wrap(Args.of(1, NULL))))
            .assertAllResolved()
            .assertItems("0", 1, NULL);

        with(Args.flattenArgsOf(wrap(Args.of(NULL)), wrap(Args.of(NULL, NULL))))
            .assertAllResolved()
            .assertItems(NULL, NULL, NULL);

        with(Args.flattenArgsOf(wrap(Args.of(NULL)), wrap(Args.of(1, 2)), wrap(Args.of(3L))))
            .assertAllResolved()
            .assertItems(NULL, 1, 2, 3L);

        with(Args.flattenArgsOf(wrap(Args.of(NULL)), wrap(Args.of(1, 2)), wrap(Args.of()), wrap(Args.of(3L))))
            .assertAllResolved()
            .assertItems(NULL, 1, 2, 3L);

        with(Args.flattenArgsOf(List.of(wrap(Args.of(NULL)), wrap(Args.of(1, 2)), wrap(Args.of()), wrap(Args.of(3L)))))
            .assertAllResolved()
            .assertItems(NULL, 1, 2, 3L);
    }

    @Test
    public void of_unresolved() {
        with(Args.of(UNRESOLVED_A))
            .assertItems(0)
            .assertUnresolved(UNRESOLVED_A);

        with(Args.of(NULL, UNRESOLVED_B))
            .assertItems(NULL, NULL)
            .assertUnresolved(UNRESOLVED_B);

        with(Args.of(UNRESOLVED_A, UNRESOLVED_B))
            .assertItems(0, NULL)
            .assertUnresolved(UNRESOLVED_A, UNRESOLVED_B);

        with(Args.of(1, UNRESOLVED_A, "2"))
            .assertItems(1, 0, "2")
            .assertUnresolved(UNRESOLVED_A);

        with(Args.of(NULL, UNRESOLVED_A, "1", UNRESOLVED_B, NULL))
            .assertItems(NULL, 0, "1", NULL, NULL)
            .assertUnresolved(UNRESOLVED_A, UNRESOLVED_B);
    }

    @Test
    public void resolveArgsByName_zero_unresolved() {
        Args args = Args.of(1, "2");
        assertThat(args.resolveArgsByName(Map.of())).isSameInstanceAs(args);
    }

    @Test
    public void resolveArgsByName_one_unresolved() {
        Args args = Args.of(1, UNRESOLVED_A, "2");
        Map<String, ?> resolved = Map.of(UNRESOLVED_A.name(), "foo");

        assertThat(args.asList()).containsExactly(1, 0, "2").inOrder();
        assertThat(args.resolveArgsByName(resolved).asList()).containsExactly(1, "foo", "2").inOrder();
    }

    @Test
    public void resolveArgsByName_two_unresolved() {
        Args args = Args.of(1, UNRESOLVED_A, "2", UNRESOLVED_B);
        Map<String, ?> resolved = Map.of(UNRESOLVED_A.name(), "foo", UNRESOLVED_B.name(), 10L);

        assertThat(args.asList()).containsExactly(1, 0, "2", null).inOrder();
        assertThat(args.resolveArgsByName(resolved).asList()).containsExactly(1, "foo", "2", 10L).inOrder();
    }

    @Test
    public void resolveArgsByName_missing_values() {
        Args args = Args.of(1, UNRESOLVED_A, "2");

        assertThrows(AssertionError.class, () -> args.resolveArgsByName(Map.of()));
        assertThrows(AssertionError.class, () -> args.resolveArgsByName(Map.of(UNRESOLVED_B.name(), "foo")));
        assertThrows(AssertionError.class, () -> args.resolveArgsByName(Map.of(UNRESOLVED_A.name(), 1, UNRESOLVED_B.name(), 2)));
    }

    @Test
    public void resolveArgsByOrderedList_zero_unresolved() {
        Args args = Args.of(1, "2");
        assertThat(args.resolveArgsByOrderedList(List.of())).isSameInstanceAs(args);
    }

    @Test
    public void resolveArgsByOrderedList_one_unresolved() {
        Args args = Args.of(1, UNRESOLVED_A, "2");
        List<String> resolved = List.of("foo");

        assertThat(args.asList()).containsExactly(1, 0, "2").inOrder();
        assertThat(args.resolveArgsByOrderedList(resolved).asList()).containsExactly(1, "foo", "2").inOrder();
    }

    @Test
    public void resolveArgsByOrderedList_two_unresolved() {
        Args args = Args.of(1, UNRESOLVED_A, "2", UNRESOLVED_B);
        List<?> resolved = List.of("foo", 10L);

        assertThat(args.asList()).containsExactly(1, 0, "2", null).inOrder();
        assertThat(args.resolveArgsByOrderedList(resolved).asList()).containsExactly(1, "foo", "2", 10L).inOrder();
    }

    @Test
    public void resolveArgsByOrderedList_all_unresolved() {
        Args args = Args.of(UNRESOLVED_A, UNRESOLVED_B);
        List<?> resolved = List.of("foo", 10L);

        assertThat(args.asList()).containsExactly(0, null).inOrder();
        assertThat(args.resolveArgsByOrderedList(resolved).asList()).containsExactly("foo", 10L).inOrder();
    }

    @Test
    public void resolveArgsByOrderedList_missing_values() {
        Args args = Args.of(1, UNRESOLVED_A, "2");

        assertThrows(AssertionError.class, () -> args.resolveArgsByOrderedList(List.of()));
        assertThrows(AssertionError.class, () -> args.resolveArgsByOrderedList(List.of("foo", "bar")));
    }

    private static @NotNull ArgsAssert with(@NotNull Args args) {
        return new ArgsAssert(args);
    }

    private static @NotNull HasArgs wrap(@NotNull Args args) {
        return new HasArgs() {
            @Override
            public @NotNull Args args() {
                return args;
            }

            @Override
            public @NotNull String repr() {
                return "dummy";
            }
        };
    }

    @CanIgnoreReturnValue
    private static class ArgsAssert {
        private final Args args;

        public ArgsAssert(@NotNull Args args) {
            this.args = args;
            assertInternalConsistency();
        }

        public @NotNull ArgsAssert assertInternalConsistency() {
            String type = getPrivateFieldValue(args, "type").toString();
            Class<?> expectedClass = switch (type) {
                case "INTS" -> IntArrayList.class;
                case "LONGS" -> LongArrayList.class;
                case "GENERIC_LIST" -> ImmutableArrayList.class;
                default -> throw new AssertionError("Unexpected args.type: " + type);
            };
            assertInternalType(expectedClass);
            return this;
        }

        public @NotNull ArgsAssert assertAllResolved() {
            assertTrue(args.isAllResolved());
            return this;
        }

        public @NotNull ArgsAssert assertUnresolved(@NotNull UnresolvedArg @NotNull ... unresolvedArgs) {
            assertFalse(args.isAllResolved());

            List<String> names = Arrays.stream(unresolvedArgs).map(UnresolvedArg::name).toList();
            List<String> upperNames = names.stream().map(String::toUpperCase).toList();
            Map<String, String> map = names.stream().collect(Collectors.toMap(name -> name, String::toUpperCase));

            Args resolvedByName = args.resolveArgsByName(map);
            assertTrue(resolvedByName.isAllResolved());
            assertThat(resolvedByName.asList()).containsAtLeastElementsIn(upperNames).inOrder();

            Args resolvedByList = args.resolveArgsByOrderedList(names);
            assertTrue(resolvedByList.isAllResolved());
            assertThat(resolvedByList.asList()).containsAtLeastElementsIn(names).inOrder();

            return this;
        }

        public @NotNull ArgsAssert assertItems(@Nullable Object @NotNull ... expected) {
            assertEquals(expected.length, args.size());
            assertEquals(expected.length == 0, args.isEmpty());
            assertThat(args.asList()).containsExactlyElementsIn(expected).inOrder();

            assertPreparedParams(expected);
            assertPreparedParamsWithOffset(expected);

            return this;
        }

        public @NotNull ArgsAssert assertPreparedParams(@Nullable Object @NotNull ... expected) {
            MockPreparedStatement statement = mockPreparedStatement();
            int added = runRethrow(() -> args.setPreparedParams(statement));
            assertEquals(added, expected.length);
            assertThat(statement).withParams().equalExactly(expected);
            return this;
        }

        public @NotNull ArgsAssert assertPreparedParamsWithOffset(@Nullable Object @NotNull ... expected) {
            MockPreparedStatement statement = mockPreparedStatement();
            int added = runRethrow(() -> {
                statement.setObject(1, null);
                return args.setPreparedParams(statement, 1);
            });
            expected = TestingBasics.prependVarArg(null, expected);
            assertEquals(added, expected.length);
            assertThat(statement).withParams().equalExactly(expected);
            return this;
        }

        public @NotNull ArgsAssert assertInternalType(@NotNull Class<?> klass) {
            assertPrivateFieldClass(args, "internal", klass);
            assertPrivateFieldClass(args, "external", klass);
            return this;
        }
    }
}
