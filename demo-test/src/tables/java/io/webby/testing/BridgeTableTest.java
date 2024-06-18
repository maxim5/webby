package io.webby.testing;

import io.webby.orm.api.BaseTable;
import io.webby.orm.api.BridgeTable;
import io.webby.testing.TestingTables.BridgeLeftIdSubject;
import io.webby.testing.TestingTables.BridgeRightIdSubject;
import io.webby.util.base.Pair;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.testing.TestingTables.withTable;

public interface BridgeTableTest
        <IL, EL, IR, ER, E, T extends BaseTable<E> & BridgeTable<IL, EL, IR, ER>>
        extends BaseTableTest<E, T> {

    @NotNull Pair<IL[], EL[]> prepareLefts(int num);

    @NotNull Pair<IR[], ER[]> prepareRights(int num);

    void prepareRelations(@NotNull List<Pair<IL, IR>> relations);

    @Test
    default void get_lefts_and_rights_2x2_empty() {
        IL[] leftKey = prepareLefts(2).first();
        IR[] rightKey = prepareRights(2).first();

        withLeftId(leftKey[0]).assertNothing();
        withLeftId(leftKey[1]).assertNothing();
        withRightId(rightKey[0]).assertNothing();
        withRightId(rightKey[1]).assertNothing();
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

        withLeftId(leftKey[0]).assertOneRight(rightKey[0], rightVal[0]);
        withLeftId(leftKey[1]).assertOneRight(rightKey[1], rightVal[1]);
        withRightId(rightKey[0]).assertOneLeft(leftKey[0], leftVal[0]);
        withRightId(rightKey[1]).assertOneLeft(leftKey[1], leftVal[1]);
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

        withLeftId(leftKey[0]).assertRightIds(rightKey[0], rightKey[1]).assertRightEntities(rightVal[0], rightVal[1]);
        withLeftId(leftKey[1]).assertOneRight(rightKey[1], rightVal[1]);
        withRightId(rightKey[0]).assertOneLeft(leftKey[0], leftVal[0]);
        withRightId(rightKey[1]).assertLeftIds(leftKey[0], leftKey[1]).assertLeftEntities(leftVal[0], leftVal[1]);
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

        withLeftId(leftKey[0]).assertOneRight(rightKey[0], rightVal[0]);
        withLeftId(leftKey[1]).assertNothing();
        withRightId(rightKey[0]).assertOneLeft(leftKey[0], leftVal[0]);
        withRightId(rightKey[1]).assertNothing();
    }

    @Test
    default void exists_2x2() {
        IL[] leftKey = prepareLefts(2).first();
        IR[] rightKey = prepareRights(2).first();
        prepareRelations(List.of(
            Pair.of(leftKey[0], rightKey[0]),
            Pair.of(leftKey[1], rightKey[1])
        ));

        assertThat(table().exists(leftKey[0], rightKey[0])).isTrue();
        assertThat(table().exists(leftKey[1], rightKey[1])).isTrue();
        assertThat(table().exists(leftKey[0], rightKey[1])).isFalse();
        assertThat(table().exists(leftKey[1], rightKey[0])).isFalse();
    }

    private @NotNull BridgeLeftIdSubject<IL, IR, ER, T> withLeftId(IL leftId) {
        return withTable(table()).forLeftId(leftId);
    }

    private @NotNull BridgeRightIdSubject<IL, EL, IR, T> withRightId(IR rightId) {
        return withTable(table()).forRightId(rightId);
    }
}
