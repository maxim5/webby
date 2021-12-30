package io.webby.testing;

import com.carrotsearch.hppc.IntArrayList;
import io.webby.db.model.IntAutoIdModel;
import io.webby.orm.api.TableInt;
import io.webby.orm.api.query.CompareType;
import io.webby.orm.api.query.HardcodedNumericTerm;
import io.webby.orm.api.query.Shortcuts;
import io.webby.orm.api.query.Where;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.testing.TestingPrimitives.newIntObjectMap;
import static io.webby.testing.TestingUtil.array;
import static org.junit.jupiter.api.Assertions.*;

public interface TableIntTest<E, T extends TableInt<E>> extends PrimaryKeyTableTest<Integer, E, T> {
    @Override
    default @NotNull Integer[] keys() {
        return array(1, 2, 3, 4, 5);
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
    default void insert_auto_id() {
        E entity = createEntity(IntAutoIdModel.AUTO_ID, 0);
        int autoId = table().insertAutoIncPk(entity);
        assertTrue(autoId > 0);
        entity = copyEntityWithId(entity, autoId);

        assertTableCount(1);
        assertEquals(entity, table().getByPkOrNull(autoId));
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
        assertEquals(entity, table().getByPkOrNull(autoId));
        assertNull(table().getByPkOrNull(key));
        assertThat(table().fetchAll()).containsExactly(entity);
    }

    @Test
    default void insert_several_auto_ids() {
        Map<Integer, E> entities = new HashMap<>();
        int num = 3;
        for (int version = 0; version < num; version++) {
            E entity = createEntity(IntAutoIdModel.AUTO_ID, version);
            int autoId = table().insertAutoIncPk(entity);
            assertTrue(autoId > 0);
            entities.put(autoId, copyEntityWithId(entity, autoId));
        }

        assertTableCount(num);
        assertThat(table().fetchAll()).containsExactlyElementsIn(entities.values());

        for (Map.Entry<Integer, E> entry : entities.entrySet()) {
            assertEquals(entry.getValue(), table().getByPkOrNull(entry.getKey()));
        }
    }

    @Test
    default void update_where_equals_variable() {
        assumeKeys(2);
        table().insert(createEntity(keys()[0], 0));

        E entity = createEntity(keys()[0], 1);
        Where where = Where.of(CompareType.EQ.compare(new HardcodedNumericTerm(findPkColumnOrDie()), Shortcuts.var(keys()[0])));
        assertEquals(1, table().updateWhere(entity, where));

        assertTableCount(1);
        assertEquals(entity, table().getByPkOrNull(keys()[0]));
        assertNull(table().getByPkOrNull(keys()[1]));
        assertThat(table().fetchAll()).containsExactly(entity);
    }

    @NotNull E copyEntityWithId(@NotNull E entity, int autoId);
}
