package io.webby.testing;

import io.webby.util.sql.api.TableObj;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.util.sql.api.ReadFollow.*;
import static org.junit.jupiter.api.Assertions.*;

public abstract class BaseModelForeignKeyTableTest<K, E, T extends TableObj<K, E>> extends BaseModelKeyTableTest<K, E, T> {
    @Test
    public void entity_no_follow() {
        assumeKeys(1);
        TableObj<K, E> table = this.table.withReferenceFollowOnRead(NO_FOLLOW);

        assertEquals(0, table.count());
        assertTrue(table.isEmpty());
        assertNull(table.getByPkOrNull(keys[0]));
        assertThat(table.fetchAll()).isEmpty();

        E shallow = createEntity(keys[0]);
        assertEquals(1, table.insert(shallow));
        assertEquals(shallow, table.getByPkOrNull(keys[0]));
        assertThat(table.fetchAll()).containsExactly(shallow);

        enrichOneLevel(shallow);
        assertEquals(1, table.count());
        assertFalse(table.isEmpty());
        assertEquals(shallow, table.getByPkOrNull(keys[0]));
        assertThat(table.fetchAll()).containsExactly(shallow);
    }

    @Test
    public void entity_follow_one_level() {
        assumeKeys(1);
        TableObj<K, E> table = this.table.withReferenceFollowOnRead(FOLLOW_ONE_LEVEL);

        assertEquals(0, table.count());
        assertTrue(table.isEmpty());
        assertNull(table.getByPkOrNull(keys[0]));
        assertThat(table.fetchAll()).isEmpty();

        E shallow = createEntity(keys[0]);
        assertEquals(1, table.insert(shallow));

        E rich = enrichOneLevel(shallow);
        assertEquals(1, table.count());
        assertFalse(table.isEmpty());
        assertEquals(rich, table.getByPkOrNull(keys[0]));
        assertThat(table.fetchAll()).containsExactly(rich);
    }

    @Test
    public void entity_follow_all_levels() {
        assumeKeys(1);
        TableObj<K, E> table = this.table.withReferenceFollowOnRead(FOLLOW_ALL);

        assertEquals(0, table.count());
        assertTrue(table.isEmpty());
        assertNull(table.getByPkOrNull(keys[0]));
        assertThat(table.fetchAll()).isEmpty();

        E shallow = createEntity(keys[0]);
        assertEquals(1, table.insert(shallow));

        E rich = enrichAllLevels(shallow);
        assertEquals(1, table.count());
        assertFalse(table.isEmpty());
        assertEquals(rich, table.getByPkOrNull(keys[0]));
        assertThat(table.fetchAll()).containsExactly(rich);
    }

    protected abstract @NotNull E enrichOneLevel(@NotNull E entity);

    protected abstract @NotNull E enrichAllLevels(@NotNull E entity);
}
