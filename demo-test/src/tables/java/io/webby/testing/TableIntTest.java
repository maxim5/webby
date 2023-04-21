package io.webby.testing;

import com.carrotsearch.hppc.IntArrayList;
import io.webby.db.model.IntAutoIdModel;
import io.webby.orm.api.TableInt;
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
import static io.webby.testing.AssertPrimitives.assertIntsNoOrder;
import static io.webby.testing.TestingBasics.array;
import static io.webby.testing.TestingPrimitives.newIntObjectMap;
import static org.junit.jupiter.api.Assertions.*;

public interface TableIntTest<E, T extends TableInt<E>> extends PrimaryKeyTableTest<Integer, E, T> {
    @Override
    default @NotNull Integer[] keys() {
        return array(1, 2, 3, 4, 5);
    }

    default int maxSupportedVersion() {
        return 2;
    }

    @Test
    default void get_by_pk_int() {
        assumeKeys(1);
        int key = keys()[0];
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
    default void getBatchByPk_ints() {
        assumeKeys(2);
        int key1 = keys()[0];
        int key2 = keys()[1];
        IntArrayList batch = IntArrayList.from(key1, key2);
        assertThat(table().getBatchByPk(batch)).isEmpty();

        E entity1 = createEntity(key1);
        assertEquals(1, table().insert(entity1));
        assertEquals(table().getBatchByPk(batch), newIntObjectMap(key1, entity1));

        E entity2 = createEntity(key2);
        assertEquals(1, table().insert(entity2));
        assertEquals(table().getBatchByPk(batch), newIntObjectMap(key1, entity1, key2, entity2));
    }

    @Test
    default void fetchPks_ints() {
        assumeKeys(2);
        int key1 = keys()[0];
        int key2 = keys()[1];
        E entity1 = createEntity(key1);
        E entity2 = createEntity(key2);
        table().insertBatch(List.of(entity1, entity2));

        assertIntsNoOrder(table().fetchPks(Where.of(Shortcuts.TRUE)), key1, key2);
        assertIntsNoOrder(table().fetchPks(Where.of(Shortcuts.lookupBy(findPkColumnOrDie(), key1))), key1);
        assertIntsNoOrder(table().fetchPks(Where.of(Shortcuts.lookupBy(findPkColumnOrDie(), key2))), key2);
        assertIntsNoOrder(table().fetchPks(Where.of(Shortcuts.FALSE)));
    }

    @Test
    default void insert_auto_id() {
        E entity = createEntity(IntAutoIdModel.AUTO_ID, 0);
        int autoId = table().insertAutoIncPk(entity);
        assertTrue(autoId > 0);
        entity = copyEntityWithId(entity, autoId);

        assertTableCount(1);
        assertTableContains(autoId, entity);
        assertThat(table().fetchAll()).containsExactly(entity);
    }

    @Test
    default void insert_auto_id_ignore_existing() {
        Integer key = 1000;
        E entity = createEntity(key, 0);
        int autoId = table().insertAutoIncPk(entity);
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
        Map<Integer, E> entities = new HashMap<>();
        int num = maxSupportedVersion() + 1;  // start from 0
        for (int version = 0; version < num; version++) {
            E entity = createEntity(IntAutoIdModel.AUTO_ID, version);
            int autoId = table().insertAutoIncPk(entity);
            assertTrue(autoId > 0);
            entities.put(autoId, copyEntityWithId(entity, autoId));
        }

        assertTableCount(num);
        assertThat(table().fetchAll()).containsExactlyElementsIn(entities.values());

        for (Map.Entry<Integer, E> entry : entities.entrySet()) {
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
    default void int_key_of() {
        assumeKeys(1);
        int key = keys()[0];
        E entity = createEntity(key);
        assertEquals(table().intKeyOf(entity), key);
        assertEquals(table().keyOf(entity), key);
    }

    @NotNull E copyEntityWithId(@NotNull E entity, int autoId);
}
