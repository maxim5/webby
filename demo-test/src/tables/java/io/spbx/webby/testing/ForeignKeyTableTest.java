package io.spbx.webby.testing;

import io.spbx.orm.api.TableObj;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.google.common.truth.Truth.assertThat;
import static io.spbx.orm.api.ReadFollow.*;

public interface ForeignKeyTableTest<K, E, T extends TableObj<K, E>> extends PrimaryKeyTableTest<K, E, T> {
    @ParameterizedTest(name = "version = {0}")
    @ValueSource(ints = {0, 1})
    default void entity_no_follow(int version) {
        assumeKeys(1);
        TableObj<K, E> table = table().withReferenceFollowOnRead(NO_FOLLOW);

        assertTableCount(0);
        assertTableNotContains(keys()[0]);
        assertThat(table.fetchAll()).isEmpty();

        E shallow = createEntity(keys()[0], version);
        assertThat(table.insert(shallow)).isEqualTo(1);
        assertTableCount(1);
        assertTableContains(keys()[0], shallow);
        assertThat(table.fetchAll()).containsExactly(shallow);

        enrichOneLevel(shallow);
        assertThat(table.getByPkOrNull(keys()[0])).isEqualTo(shallow);
        assertThat(table.fetchAll()).containsExactly(shallow);
    }

    @ParameterizedTest(name = "version = {0}")
    @ValueSource(ints = {0, 1})
    default void entity_follow_one_level(int version) {
        assumeKeys(1);
        TableObj<K, E> table = table().withReferenceFollowOnRead(FOLLOW_ONE_LEVEL);

        assertTableCount(0);
        assertTableNotContains(keys()[0]);
        assertThat(table.fetchAll()).isEmpty();

        E shallow = createEntity(keys()[0], version);
        assertThat(table.insert(shallow)).isEqualTo(1);
        assertTableCount(1);

        E rich = enrichOneLevel(shallow);
        assertThat(table.getByPkOrNull(keys()[0])).isEqualTo(rich);
        assertThat(table.fetchAll()).containsExactly(rich);
    }

    @ParameterizedTest(name = "version = {0}")
    @ValueSource(ints = {0, 1})
    default void entity_follow_all_levels(int version) {
        assumeKeys(1);
        TableObj<K, E> table = table().withReferenceFollowOnRead(FOLLOW_ALL);

        assertTableCount(0);
        assertTableNotContains(keys()[0]);
        assertThat(table.fetchAll()).isEmpty();

        E shallow = createEntity(keys()[0], version);
        assertThat(table.insert(shallow)).isEqualTo(1);
        assertTableCount(1);

        E rich = enrichAllLevels(shallow);
        assertThat(table.getByPkOrNull(keys()[0])).isEqualTo(rich);
        assertThat(table.fetchAll()).containsExactly(rich);
    }

    @NotNull E enrichOneLevel(@NotNull E entity);

    @NotNull E enrichAllLevels(@NotNull E entity);
}
