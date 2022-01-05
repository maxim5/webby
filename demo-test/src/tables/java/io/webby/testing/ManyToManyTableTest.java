package io.webby.testing;

import io.webby.orm.api.BaseTable;
import io.webby.orm.api.ManyToManyTable;
import io.webby.util.collect.Pair;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
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

        assertLefts(rightKey[0]);
        assertLefts(rightKey[1]);
        assertRights(leftKey[0]);
        assertRights(leftKey[1]);
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

        assertLefts(rightKey[0], leftVal[0]);
        assertLefts(rightKey[1], leftVal[1]);
        assertRights(leftKey[0], rightVal[0]);
        assertRights(leftKey[1], rightVal[1]);
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

        assertLefts(rightKey[0], leftVal[0]);
        assertLefts(rightKey[1], leftVal[0], leftVal[1]);
        assertRights(leftKey[0], rightVal[0], rightVal[1]);
        assertRights(leftKey[1], rightVal[1]);
    }

    @Test
    default void get_lefts_and_rights_2x2_unused() {
        IL[] leftKey = prepareLefts(2).first();
        EL[] leftVal = prepareLefts(2).second();
        IR[] rightKey = prepareRights(2).first();
        ER[] rightVal = prepareRights(2).second();
        prepareRelations(List.of(
            Pair.of(leftKey[0], rightKey[0])
        ));

        assertLefts(rightKey[0], leftVal[0]);
        assertLefts(rightKey[1]);
        assertRights(leftKey[0], rightVal[0]);
        assertRights(leftKey[1]);
    }

    @SafeVarargs
    private void assertLefts(@NotNull IR rightIndex, @NotNull EL @NotNull... entities) {
        assertEquals(entities.length == 0, table().rightExists(rightIndex));
        assertEquals(entities.length, table().countLefts(rightIndex));
        List<EL> each = new ArrayList<>();
        table().forEachLeft(rightIndex, each::add);
        assertThat(each).containsExactlyElementsIn(entities);
        assertThat(table().fetchAllLefts(rightIndex)).containsExactlyElementsIn(entities);
    }

    @SafeVarargs
    private void assertRights(@NotNull IL leftIndex, @NotNull ER @NotNull ... entities) {
        assertEquals(entities.length == 0, table().leftExists(leftIndex));
        assertEquals(entities.length, table().countRights(leftIndex));
        List<ER> each = new ArrayList<>();
        table().forEachRight(leftIndex, each::add);
        assertThat(each).containsExactlyElementsIn(entities);
        assertThat(table().fetchAllRights(leftIndex)).containsExactlyElementsIn(entities);
    }
}
