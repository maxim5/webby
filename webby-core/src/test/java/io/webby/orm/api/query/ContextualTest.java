package io.webby.orm.api.query;

import com.carrotsearch.hppc.IntArrayList;
import io.webby.orm.testing.FakeColumn;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.orm.api.query.Shortcuts.*;
import static io.webby.orm.testing.AssertSql.assertArgs;
import static io.webby.orm.testing.AssertSql.assertRepr;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ContextualTest {
    @Test
    public void invalid_resolver_map_is_empty() {
        AssertionError e = assertThrows(AssertionError.class, () ->
            Contextual.<Where, IntArrayList>resolvingByName(
                Where.of(lookupBy(FakeColumn.INT, unresolved("U", TermType.NUMBER))),
                array -> Map.of()
            ).resolveQueryArgs(IntArrayList.from(1))
        );
        assertThat(e).hasMessageThat().contains("Provided resolved args size doesn't match unresolved placeholders");
    }

    @Test
    public void invalid_resolver_map_is_larger() {
        AssertionError e = assertThrows(AssertionError.class, () ->
            Contextual.<Where, IntArrayList>resolvingByName(
                Where.of(lookupBy(FakeColumn.INT, unresolved("U", TermType.NUMBER))),
                array -> Map.of("U", array.get(0), "V", array.get(0))
            ).resolveQueryArgs(IntArrayList.from(1))
        );
        assertThat(e).hasMessageThat().contains("Provided resolved args size doesn't match unresolved placeholders");
    }

    @Test
    public void invalid_resolver_map_has_wrong_name() {
        AssertionError e = assertThrows(AssertionError.class, () ->
            Contextual.<Where, IntArrayList>resolvingByName(
                Where.of(lookupBy(FakeColumn.INT, unresolved("U", TermType.NUMBER))),
                array -> Map.of("V", array.get(0))
            ).resolveQueryArgs(IntArrayList.from(1))
        );
        assertThat(e).hasMessageThat().contains("Provided resolved args keys don't match unresolved placeholders");
    }

    @Test
    public void invalid_resolver_list_is_empty() {
        AssertionError e = assertThrows(AssertionError.class, () ->
            Contextual.<Where, IntArrayList>resolvingByOrderedList(
                Where.of(lookupBy(FakeColumn.INT, unresolved("U", TermType.NUMBER))),
                array -> List.of()
            ).resolveQueryArgs(IntArrayList.from(1))
        );
        assertThat(e).hasMessageThat().contains("Provided resolved args size doesn't match unresolved placeholders");
    }

    @Test
    public void invalid_resolver_list_is_larger() {
        AssertionError e = assertThrows(AssertionError.class, () ->
            Contextual.<Where, IntArrayList>resolvingByOrderedList(
                Where.of(lookupBy(FakeColumn.INT, unresolved("U", TermType.NUMBER))),
                array -> List.of(array.get(0), array.get(0))
            ).resolveQueryArgs(IntArrayList.from(1))
        );
        assertThat(e).hasMessageThat().contains("Provided resolved args size doesn't match unresolved placeholders");
    }

    @Test
    public void one_unresolved_ByName_int_array() {
        Contextual<Where, IntArrayList> contextual = Contextual.resolvingByName(
            Where.of(lookupBy(FakeColumn.INT, unresolved("U", TermType.NUMBER))),
            array -> Map.of("U", array.get(0))  // takes first
        );
        assertRepr(contextual, "WHERE i = ?");
        assertArgs(contextual.resolveQueryArgs(IntArrayList.from(11)), 11);
        assertArgs(contextual.resolveQueryArgs(IntArrayList.from(11, 12)), 11);
    }

    @Test
    public void one_unresolved_ByOrder_int_array() {
        Contextual<Where, IntArrayList> contextual = Contextual.resolvingByOrderedList(
            Where.of(lookupBy(FakeColumn.INT, unresolved("U", TermType.NUMBER))),
            array -> List.of(array.get(0))  // takes first
        );
        assertRepr(contextual, "WHERE i = ?");
        assertArgs(contextual.resolveQueryArgs(IntArrayList.from(11, 12)), 11);
        assertArgs(contextual.resolveQueryArgs(IntArrayList.from(11, 12, 13)), 11);
    }

    @Test
    public void one_unresolved_and_other_args_ByOrder_int_array() {
        Contextual<Where, IntArrayList> contextual = Contextual.resolvingByOrderedList(
            Where.and(between(FakeColumn.INT, unresolved("U", TermType.NUMBER), var(100))),
            array -> List.of(array.get(1))  // takes second
        );
        assertRepr(contextual, "WHERE i BETWEEN ? AND ?");
        assertArgs(contextual.resolveQueryArgs(IntArrayList.from(11, 12)), 12, 100);
        assertArgs(contextual.resolveQueryArgs(IntArrayList.from(11, 12, 13)), 12, 100);
    }

    @Test
    public void two_unresolved_ByName_int_array() {
        Contextual<Where, IntArrayList> contextual = Contextual.resolvingByName(
            Where.and(between(FakeColumn.INT, unresolved("U", TermType.NUMBER), unresolved("V", TermType.NUMBER))),
            array -> Map.of("U", array.get(1), "V", array.get(2))  // takes second and third
        );
        assertRepr(contextual, "WHERE i BETWEEN ? AND ?");
        assertArgs(contextual.resolveQueryArgs(IntArrayList.from(11, 12, 13)), 12, 13);
        assertArgs(contextual.resolveQueryArgs(IntArrayList.from(11, 12, 13, 14)), 12, 13);
    }

    @Test
    public void two_unresolved_ByName_int_array_duplicate_value() {
        Contextual<Where, IntArrayList> contextual = Contextual.resolvingByName(
            Where.and(between(FakeColumn.INT, unresolved("U", TermType.NUMBER), unresolved("V", TermType.NUMBER))),
            array -> Map.of("U", array.get(1), "V", array.get(1))  // takes second twice!
        );
        assertRepr(contextual, "WHERE i BETWEEN ? AND ?");
        assertArgs(contextual.resolveQueryArgs(IntArrayList.from(11, 12, 13)), 12, 12);
        assertArgs(contextual.resolveQueryArgs(IntArrayList.from(11, 12, 13, 14)), 12, 12);
    }

    @Test
    public void two_unresolved_ByOrder_int_array_duplicate_value() {
        Contextual<Where, IntArrayList> contextual = Contextual.resolvingByOrderedList(
            Where.and(between(FakeColumn.INT, unresolved("U", TermType.NUMBER), unresolved("V", TermType.NUMBER))),
            array -> List.of(array.get(0), array.get(0))  // takes first twice!
        );
        assertRepr(contextual, "WHERE i BETWEEN ? AND ?");
        assertArgs(contextual.resolveQueryArgs(IntArrayList.from(11, 12)), 11, 11);
        assertArgs(contextual.resolveQueryArgs(IntArrayList.from(11, 12, 13)), 11, 11);
    }
}
