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

import static com.google.common.truth.Truth.assertThat;
import static io.spbx.util.testing.AssertHppc.assertArray;
import static io.spbx.util.testing.AssertHppc.assertMap;
import static io.spbx.util.testing.TestingBasics.array;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        assertThat(table().getOptionalByPk(key)).isEmpty();
        assertThrows(NullPointerException.class, () -> table().getByPkOrDie(key));

        assertThat(table().insert(entity)).isEqualTo(1);
        assertThat(table().getByPkOrNull(key)).isEqualTo(entity);
        assertThat(table().getByPkOrDie(key)).isEqualTo(entity);
        assertThat(table().getOptionalByPk(key)).hasValue(entity);
    }

    @Test
    default void getBatchByPk_ints() {
        assumeKeys(2);
        int key1 = keys()[0];
        int key2 = keys()[1];
        IntArrayList batch = IntArrayList.from(key1, key2);
        assertMap(table().getBatchByPk(batch)).isEmpty();

        E entity1 = createEntity(key1);
        assertThat(table().insert(entity1)).isEqualTo(1);
        assertMap(table().getBatchByPk(batch)).containsExactly(key1, entity1);

        E entity2 = createEntity(key2);
        assertThat(table().insert(entity2)).isEqualTo(1);
        assertMap(table().getBatchByPk(batch)).containsExactly(key1, entity1, key2, entity2);
    }

    @Test
    default void fetchPks_ints() {
        assumeKeys(2);
        int key1 = keys()[0];
        int key2 = keys()[1];
        E entity1 = createEntity(key1);
        E entity2 = createEntity(key2);
        table().insertBatch(List.of(entity1, entity2));

        assertArray(table().fetchPks(Where.of(Shortcuts.TRUE))).containsExactlyNoOrder(key1, key2);
        assertArray(table().fetchPks(Where.of(Shortcuts.lookupBy(findPkColumnOrDie(), key1)))).containsExactlyNoOrder(key1);
        assertArray(table().fetchPks(Where.of(Shortcuts.lookupBy(findPkColumnOrDie(), key2)))).containsExactlyNoOrder(key2);
        assertArray(table().fetchPks(Where.of(Shortcuts.FALSE))).containsExactlyNoOrder();
    }

    @Test
    default void insert_auto_id() {
        E entity = createEntity(IntAutoIdModel.AUTO_ID, 0);
        int autoId = table().insertAutoIncPk(entity);
        assertThat(autoId > 0).isTrue();
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
        assertThat(autoId > 0).isTrue();
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
            assertThat(autoId > 0).isTrue();
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
        assertThat(table().updateWhere(entity, where)).isEqualTo(1);

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
        assertThat(table().intKeyOf(entity)).isEqualTo(key);
        assertThat(table().keyOf(entity)).isEqualTo(key);
    }

    @NotNull E copyEntityWithId(@NotNull E entity, int autoId);
}
