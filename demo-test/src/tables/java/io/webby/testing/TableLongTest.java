package io.webby.testing;

import com.carrotsearch.hppc.LongArrayList;
import io.webby.db.model.LongAutoIdModel;
import io.webby.orm.api.TableLong;
import io.webby.orm.api.query.CompareType;
import io.webby.orm.api.query.Shortcuts;
import io.webby.orm.api.query.Where;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.testing.AssertPrimitives.assertMap;
import static io.webby.testing.AssertPrimitives.assertThat;
import static io.webby.testing.TestingBasics.array;
import static org.junit.jupiter.api.Assertions.*;

public interface TableLongTest<E, T extends TableLong<E>> extends PrimaryKeyTableTest<Long, E, T> {
    @Override
    default @NotNull Long[] keys() {
        return array(1L, 2L, 3L, 4L, 5L);
    }

    default int maxSupportedVersion() {
        return 2;
    }

    @Test
    default void get_by_pk_long() {
        assumeKeys(1);
        long key = keys()[0];
        E entity = createEntity(key);

        assertThat(table().getByPkOrNull(key)).isNull();
        assertThrows(NullPointerException.class, () -> table().getByPkOrDie(key));
        assertThat(table().getOptionalByPk(key)).isEqualTo(Optional.empty());

        assertThat(table().insert(entity)).isEqualTo(1);
        assertThat(table().getByPkOrNull(key)).isEqualTo(entity);
        assertThat(table().getByPkOrDie(key)).isEqualTo(entity);
        assertThat(table().getOptionalByPk(key)).isEqualTo(Optional.of(entity));
    }

    @Test
    default void getBatchByPk_longs() {
        assumeKeys(2);
        long key1 = keys()[0];
        long key2 = keys()[1];
        LongArrayList batch = LongArrayList.from(key1, key2);
        assertThat(table().getBatchByPk(batch)).isEmpty();

        E entity1 = createEntity(key1);
        assertThat(table().insert(entity1)).isEqualTo(1);
        assertMap(table().getBatchByPk(batch)).containsExactly(key1, entity1);

        E entity2 = createEntity(key2);
        assertThat(table().insert(entity2)).isEqualTo(1);
        assertMap(table().getBatchByPk(batch)).containsExactly(key1, entity1, key2, entity2);
    }

    @Test
    default void fetchPks_longs() {
        assumeKeys(2);
        long key1 = keys()[0];
        long key2 = keys()[1];
        E entity1 = createEntity(key1);
        E entity2 = createEntity(key2);
        table().insertBatch(List.of(entity1, entity2));

        assertThat(table().fetchPks(Where.of(Shortcuts.TRUE))).containsExactlyNoOrder(key1, key2);
        assertThat(table().fetchPks(Where.of(Shortcuts.lookupBy(findPkColumnOrDie(), key1)))).containsExactlyNoOrder(key1);
        assertThat(table().fetchPks(Where.of(Shortcuts.lookupBy(findPkColumnOrDie(), key2)))).containsExactlyNoOrder(key2);
        assertThat(table().fetchPks(Where.of(Shortcuts.FALSE))).containsExactlyNoOrder();
    }

    @Test
    default void insert_auto_id() {
        E entity = createEntity(LongAutoIdModel.AUTO_ID, 0);
        long autoId = table().insertAutoIncPk(entity);
        assertTrue(autoId > 0);
        entity = copyEntityWithId(entity, autoId);

        assertTableCount(1);
        assertTableContains(autoId, entity);
        assertThat(table().fetchAll()).containsExactly(entity);
    }

    @Test
    default void insert_auto_id_ignore_existing() {
        Long key = 1000L;
        E entity = createEntity(key, 0);
        long autoId = table().insertAutoIncPk(entity);
        assertTrue(autoId > 0);
        assertNotEquals(autoId, key);
        entity = copyEntityWithId(entity, autoId);

        assertTableCount(1);
        assertTableContains(autoId, entity);
        assertTableNotContains(key);
        assertThat(table().fetchAll()).containsExactly(entity);
    }

    @Test
    default void insert_several_auto_ids() {
        Map<Long, E> entities = new HashMap<>();
        int num = maxSupportedVersion() + 1;  // start from 0
        for (int version = 0; version < num; version++) {
            E entity = createEntity(LongAutoIdModel.AUTO_ID, version);
            long autoId = table().insertAutoIncPk(entity);
            assertTrue(autoId > 0);
            entities.put(autoId, copyEntityWithId(entity, autoId));
        }

        assertTableCount(num);
        assertThat(table().fetchAll()).containsExactlyElementsIn(entities.values());

        for (Map.Entry<Long, E> entry : entities.entrySet()) {
            assertTableContains(entry.getKey(), entry.getValue());
        }
    }

    @Test
    default void update_where_equals_variable() {
        assumeKeys(2);
        table().insert(createEntity(keys()[0], 0));

        E entity = createEntity(keys()[0], 1);
        Where where = Where.of(CompareType.EQ.compare(findPkColumnOrDie(), Shortcuts.var(keys()[0])));
        assertThat(table().updateWhere(entity, where)).isEqualTo(1);

        assertTableCount(1);
        assertTableContains(keys()[0], entity);
        assertTableNotContains(keys()[1]);
        assertThat(table().fetchAll()).containsExactly(entity);
    }

    @Test
    default void long_key_of() {
        assumeKeys(1);
        long key = keys()[0];
        E entity = createEntity(key);
        assertThat(table().longKeyOf(entity)).isEqualTo(key);
        assertThat(table().keyOf(entity)).isEqualTo(key);
    }

    @NotNull E copyEntityWithId(@NotNull E entity, long autoId);
}
