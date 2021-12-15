package io.webby.testing;

import io.webby.orm.api.Page;
import io.webby.orm.api.TableObj;
import io.webby.orm.api.query.CompositeFilter;
import io.webby.orm.api.query.Pagination;
import io.webby.orm.api.query.Where;
import io.webby.util.collect.EasyIterables;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public interface PrimaryKeyTableTest<K, E, T extends TableObj<K, E>> extends BaseTableTest<E, T> {
    @NotNull K[] keys();

    @Override
    @Test
    default void empty() {
        assumeKeys(1);
        assertTableCount(0);
        assertNull(table().getByPkOrNull(keys()[0]));
        assertThat(table().fetchAll()).isEmpty();
    }

    @Test
    default void insert_entity() {
        assumeKeys(2);
        E entity = createEntity(keys()[0]);
        assertEquals(1, table().insert(entity));

        assertTableCount(1);
        assertEquals(entity, table().getByPkOrNull(keys()[0]));
        assertNull(table().getByPkOrNull(keys()[1]));
        assertThat(table().fetchAll()).containsExactly(entity);
    }

    @Test
    default void insert_two_entities() {
        assumeKeys(2);
        E entity1 = createEntity(keys()[0]);
        assertEquals(1, table().insert(entity1));
        E entity2 = createEntity(keys()[1]);
        assertEquals(1, table().insert(entity2));

        assertTableCount(2);
        assertEquals(entity1, table().getByPkOrNull(keys()[0]));
        assertEquals(entity2, table().getByPkOrNull(keys()[1]));
        assertThat(table().fetchAll()).containsExactly(entity1, entity2);
    }

    @Test
    default void update_entity() {
        assumeKeys(2);
        table().insert(createEntity(keys()[0], 0));
        E entity = createEntity(keys()[0], 1);
        assertEquals(1, table().updateByPk(entity));

        assertTableCount(1);
        assertEquals(entity, table().getByPkOrNull(keys()[0]));
        assertNull(table().getByPkOrNull(keys()[1]));
        assertThat(table().fetchAll()).containsExactly(entity);
    }

    @Test
    default void update_missing_entity() {
        assumeKeys(1);
        E entity = createEntity(keys()[0]);
        assertEquals(0, table().updateByPk(entity));

        assertTableCount(0);
        assertNull(table().getByPkOrNull(keys()[0]));
        assertThat(table().fetchAll()).isEmpty();
    }

    @Test
    default void update_or_insert_new_entity() {
        assumeKeys(2);
        E entity = createEntity(keys()[0]);
        assertEquals(1, table().updateByPkOrInsert(entity));

        assertTableCount(1);
        assertEquals(entity, table().getByPkOrNull(keys()[0]));
        assertNull(table().getByPkOrNull(keys()[1]));
        assertThat(table().fetchAll()).containsExactly(entity);
    }

    @Test
    default void update_or_insert_existing_entity() {
        assumeKeys(2);
        table().insert(createEntity(keys()[0], 0));
        E entity = createEntity(keys()[0], 1);
        assertEquals(1, table().updateByPkOrInsert(entity));

        assertTableCount(1);
        assertEquals(entity, table().getByPkOrNull(keys()[0]));
        assertNull(table().getByPkOrNull(keys()[1]));
        assertThat(table().fetchAll()).containsExactly(entity);
    }

    @Test
    default void delete_entity() {
        assumeKeys(1);
        table().insert(createEntity(keys()[0], 0));
        assertEquals(1, table().deleteByPk(keys()[0]));

        assertTableCount(0);
        assertNull(table().getByPkOrNull(keys()[0]));
        assertThat(table().fetchAll()).isEmpty();
    }

    @Test
    default void delete_missing_entity() {
        assumeKeys(1);
        assertEquals(0, table().deleteByPk(keys()[0]));

        assertTableCount(0);
        assertNull(table().getByPkOrNull(keys()[0]));
        assertThat(table().fetchAll()).isEmpty();
    }

    @Test
    default void fetch_page() {
        assumeKeys(2);
        E entity1 = createEntity(keys()[0]);
        assertEquals(1, table().insert(entity1));
        E entity2 = createEntity(keys()[1]);
        assertEquals(1, table().insert(entity2));

        CompositeFilter firstPageClause = CompositeFilter.builder().with(Pagination.firstPage(1), table().engine()).build();
        Page<E> page1 = table().fetchPage(firstPageClause);
        assertThat(page1.hasNextPage()).isTrue();
        assertNotNull(page1.nextToken());
        assertThat(page1.nextToken().offset()).isEqualTo(1);
        assertThat(page1.items()).hasSize(1);
        assertThat(page1.items()).containsAnyOf(entity1, entity2);

        CompositeFilter secondPageClause = CompositeFilter.builder().with(Pagination.ofOffset(1, 1), table().engine()).build();
        Page<E> page2 = table().fetchPage(secondPageClause);
        assertThat(page2.hasNextPage()).isTrue();  // in fact, is false...
        assertThat(page2.items()).hasSize(1);
        assertThat(page2.items()).containsAnyOf(entity1, entity2);

        assertThat(EasyIterables.concat(page1.items(), page2.items())).containsExactly(entity1, entity2);

        CompositeFilter thirdPageClause = CompositeFilter.builder().with(Pagination.ofOffset(2, 1), table().engine()).build();
        Page<E> page3 = table().fetchPage(thirdPageClause);
        assertThat(page3.hasNextPage()).isFalse();
        assertThat(page3.items()).isEmpty();
    }

    @NotNull E createEntity(@NotNull K key, int version);

    default @NotNull E createEntity(@NotNull K key) {
        return createEntity(key, 0);
    }

    default void assumeKeys(int minimumNum) {
        assumeTrue(keys().length >= minimumNum,
                   "Can't run the test because not enough keys available: %s".formatted(Arrays.toString(keys())));
    }

    default void assertTableCount(int count) {
        assertEquals(count, table().count());
        assertEquals(count == 0, table().isEmpty());
        assertEquals(count > 0, table().isNotEmpty());

        assertEquals(count, table().count(Where.hardcoded("1 = 1")));
        assertEquals(count > 0, table().exists(Where.hardcoded("1 = 1")));

        assertEquals(0, table().count(Where.hardcoded("0 = 1")));
        assertFalse(table().exists(Where.hardcoded("0 = 1")));
    }
}
