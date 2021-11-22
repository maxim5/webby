package io.webby.testing;

import io.webby.db.model.IntAutoIdModel;
import io.webby.orm.api.TableInt;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.testing.TestingUtil.array;
import static org.junit.jupiter.api.Assertions.*;

public interface TableIntTest<E, T extends TableInt<E>> extends PrimaryKeyTableTest<Integer, E, T> {
    @Override
    default @NotNull Integer[] keys() {
        return array(1, 2, 3, 4, 5);
    }

    @Test
    default void insert_auto_id() {
        E entity = createEntity(IntAutoIdModel.AUTO_ID, 0);
        int autoId = table().insertAutoIncPk(entity);
        assertTrue(autoId > 0);
        entity = copyEntityWithId(entity, autoId);

        assertEquals(1, table().count());
        assertFalse(table().isEmpty());
        assertEquals(entity, table().getByPkOrNull(autoId));
        assertThat(table().fetchAll()).containsExactly(entity);
    }

    @Test
    default void insert_auto_id_ignore_existing() {
        Integer key = keys()[3];
        E entity = createEntity(key, 0);
        int autoId = table().insertAutoIncPk(entity);
        assertTrue(autoId > 0);
        assertNotEquals(autoId, key);
        entity = copyEntityWithId(entity, autoId);

        assertEquals(1, table().count());
        assertFalse(table().isEmpty());
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

        assertEquals(num, table().count());
        assertFalse(table().isEmpty());
        assertThat(table().fetchAll()).containsExactlyElementsIn(entities.values());

        for (Map.Entry<Integer, E> entry : entities.entrySet()) {
            assertEquals(entry.getValue(), table().getByPkOrNull(entry.getKey()));
        }
    }

    @NotNull E copyEntityWithId(@NotNull E entity, int autoId);
}