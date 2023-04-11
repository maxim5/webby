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
import static io.webby.testing.AssertPrimitives.assertLongsNoOrder;
import static io.webby.testing.TestingPrimitives.newLongObjectMap;
import static io.webby.testing.TestingUtil.array;
import static org.junit.jupiter.api.Assertions.*;

public interface TableLongTest<E, T extends TableLong<E>> extends PrimaryKeyTableTest<Long, E, T> {
    @Override
    default @NotNull Long[] keys() {
        return array(1L, 2L, 3L, 4L, 5L);
    }

    @Test
    default void get_by_pk_long() {
        assumeKeys(1);
        long key = keys()[0];
        E entity = createEntity(key);

        assertThat(table().getByPkOrNull(key)).isNull();
        assertThrows(NullPointerException.class, () -> table().getByPkOrDie(key));
        assertThat(table().getOptionalByPk(key)).isEqualTo(Optional.empty());

        assertEquals(1, table().insert(entity));
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
        assertEquals(1, table().insert(entity1));
        assertEquals(table().getBatchByPk(batch), newLongObjectMap(key1, entity1));

        E entity2 = createEntity(key2);
        assertEquals(1, table().insert(entity2));
        assertEquals(table().getBatchByPk(batch), newLongObjectMap(key1, entity1, key2, entity2));
    }

    @Test
    default void fetchPks_longs() {
        assumeKeys(2);
        long key1 = keys()[0];
        long key2 = keys()[1];
        E entity1 = createEntity(key1);
        E entity2 = createEntity(key2);
        table().insertBatch(List.of(entity1, entity2));

        assertLongsNoOrder(table().fetchPks(Where.of(Shortcuts.TRUE)), key1, key2);
        assertLongsNoOrder(table().fetchPks(Where.of(Shortcuts.lookupBy(findPkColumnOrDie(), key1))), key1);
        assertLongsNoOrder(table().fetchPks(Where.of(Shortcuts.lookupBy(findPkColumnOrDie(), key2))), key2);
        assertLongsNoOrder(table().fetchPks(Where.of(Shortcuts.FALSE)));
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
        int num = 3;
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
        assertEquals(1, table().updateWhere(entity, where));

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
        assertEquals(table().longKeyOf(entity), key);
        assertEquals(table().keyOf(entity), key);
    }

    @NotNull E copyEntityWithId(@NotNull E entity, long autoId);
}
