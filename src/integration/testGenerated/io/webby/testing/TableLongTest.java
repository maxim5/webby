package io.webby.testing;

import io.webby.db.model.LongAutoIdModel;
import io.webby.orm.api.TableLong;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.testing.TestingUtil.array;
import static org.junit.jupiter.api.Assertions.*;

public interface TableLongTest<E, T extends TableLong<E>> extends PrimaryKeyTableTest<Long, E, T> {
    @Override
    default @NotNull Long[] keys() {
        return array(1L, 2L, 3L, 4L, 5L);
    }

    @Test
    default void insert_auto_id() {
        E entity = createEntity(LongAutoIdModel.AUTO_ID, 0);
        long autoId = table().insertAutoIncPk(entity);
        assertTrue(autoId > 0);
        entity = copyEntityWithId(entity, autoId);

        assertTableCount(1);
        assertEquals(entity, table().getByPkOrNull(autoId));
        assertThat(table().fetchAll()).containsExactly(entity);
    }

    @Test
    default void insert_auto_id_ignore_existing() {
        Long key = keys()[3];
        E entity = createEntity(key, 0);
        long autoId = table().insertAutoIncPk(entity);
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
            assertEquals(entry.getValue(), table().getByPkOrNull(entry.getKey()));
        }
    }

    @NotNull E copyEntityWithId(@NotNull E entity, long autoId);
}
