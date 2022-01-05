package io.webby.testing;

import io.webby.orm.api.BaseTable;
import io.webby.orm.api.ManyToManyTable;
import io.webby.util.collect.Pair;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public interface ManyToManyTableTest
        <IL, EL, IR, ER, E, T extends BaseTable<E> & ManyToManyTable<IL, EL, IR, ER>>
        extends BaseTableTest<E, T> {

    @NotNull Pair<IL[], EL[]> prepareLefts(int num);

    @NotNull Pair<IR[], ER[]> prepareRights(int num);

    void prepareRelations(@NotNull List<Pair<IL, IR>> relations);

    @Test
    default void get_lefts_and_rights_2x2_empty() {
        IL[] leftKey = prepareLefts(2).first();
        IR[] rightKey = prepareRights(2).first();

        assertEquals(0, table().countLefts(rightKey[0]));
        assertThat(table().fetchAllLefts(rightKey[0])).isEmpty();

        assertEquals(0, table().countLefts(rightKey[1]));
        assertThat(table().fetchAllLefts(rightKey[1])).isEmpty();

        assertEquals(0, table().countRights(leftKey[0]));
        assertThat(table().fetchAllRights(leftKey[0])).isEmpty();

        assertEquals(0, table().countRights(leftKey[1]));
        assertThat(table().fetchAllRights(leftKey[1])).isEmpty();
    }

    @Test
    default void get_lefts_and_rights_2x2_parallel() {
        IL[] leftKey = prepareLefts(2).first();
        EL[] leftVal = prepareLefts(2).second();
        IR[] rightKey = prepareRights(2).first();
        ER[] rightVal = prepareRights(2).second();
        prepareRelations(List.of(
            Pair.of(leftKey[0], rightKey[0]),
            Pair.of(leftKey[1], rightKey[1])
        ));

        assertEquals(1, table().countLefts(rightKey[0]));
        assertThat(table().fetchAllLefts(rightKey[0])).containsExactly(leftVal[0]);

        assertEquals(1, table().countLefts(rightKey[1]));
        assertThat(table().fetchAllLefts(rightKey[1])).containsExactly(leftVal[1]);

        assertEquals(1, table().countRights(leftKey[0]));
        assertThat(table().fetchAllRights(leftKey[0])).containsExactly(rightVal[0]);

        assertEquals(1, table().countRights(leftKey[1]));
        assertThat(table().fetchAllRights(leftKey[1])).containsExactly(rightVal[1]);
    }

    @Test
    default void get_lefts_and_rights_2x2_with_cross_relations() {
        IL[] leftKey = prepareLefts(2).first();
        EL[] leftVal = prepareLefts(2).second();
        IR[] rightKey = prepareRights(2).first();
        ER[] rightVal = prepareRights(2).second();
        prepareRelations(List.of(
            Pair.of(leftKey[0], rightKey[0]),
            Pair.of(leftKey[0], rightKey[1]),
            Pair.of(leftKey[1], rightKey[1])
        ));

        assertEquals(1, table().countLefts(rightKey[0]));
        assertThat(table().fetchAllLefts(rightKey[0])).containsExactly(leftVal[0]);

        assertEquals(2, table().countLefts(rightKey[1]));
        assertThat(table().fetchAllLefts(rightKey[1])).containsExactly(leftVal[0], leftVal[1]);

        assertEquals(2, table().countRights(leftKey[0]));
        assertThat(table().fetchAllRights(leftKey[0])).containsExactly(rightVal[0], rightVal[1]);

        assertEquals(1, table().countRights(leftKey[1]));
        assertThat(table().fetchAllRights(leftKey[1])).containsExactly(rightVal[1]);
    }

    @Test
    default void get_lefts_and_rights_2x2_unused() {
        IL[] leftKey = prepareLefts(3).first();
        EL[] leftVal = prepareLefts(3).second();
        IR[] rightKey = prepareRights(3).first();
        ER[] rightVal = prepareRights(3).second();
        prepareRelations(List.of(
            Pair.of(leftKey[0], rightKey[0])
        ));

        assertEquals(1, table().countLefts(rightKey[0]));
        assertThat(table().fetchAllLefts(rightKey[0])).containsExactly(leftVal[0]);

        assertEquals(0, table().countLefts(rightKey[1]));
        assertThat(table().fetchAllLefts(rightKey[1])).isEmpty();

        assertEquals(1, table().countRights(leftKey[0]));
        assertThat(table().fetchAllRights(leftKey[0])).containsExactly(rightVal[0]);

        assertEquals(0, table().countRights(leftKey[1]));
        assertThat(table().fetchAllRights(leftKey[1])).isEmpty();
    }
}
