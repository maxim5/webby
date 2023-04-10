package io.webby.testing;

import io.webby.orm.api.*;
import io.webby.orm.api.query.*;
import io.webby.util.collect.ListBuilder;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.testing.AssertBasics.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public interface PrimaryKeyTableTest<K, E, T extends TableObj<K, E>> extends BaseTableTest<E, T> {
    @NotNull K[] keys();

    @Override
    @Test
    default void empty() {
        assumeKeys(1);
        assertTableCount(0);
        assertTableNotContains(keys()[0]);
        assertThat(table().fetchAll()).isEmpty();
    }

    @Test
    default void exists() {
        assumeKeys(1);
        assertFalse(table().exists(Where.of(Shortcuts.TRUE)));
        E entity = createEntity(keys()[0]);
        assertEquals(1, table().insert(entity));
        assertTrue(table().exists(Where.of(Shortcuts.TRUE)));
    }

    @Test
    default void keyOf() {
        assumeKeys(1);
        E entity = createEntity(keys()[0]);
        assertEquals(table().keyOf(entity), keys()[0]);
    }

    /** {@link TableObj#getBatchByPk(Collection)} **/

    @Test
    default void getBatchByPk() {
        assumeKeys(2);
        K key1 = keys()[0];
        K key2 = keys()[1];
        assertMapContents(table().getBatchByPk(List.of(key1, key2)));

        E entity1 = createEntity(key1);
        assertEquals(1, table().insert(entity1));
        assertMapContents(table().getBatchByPk(List.of(key1, key2)), key1, entity1);

        E entity2 = createEntity(key2);
        assertEquals(1, table().insert(entity2));
        assertMapContents(table().getBatchByPk(List.of(key1, key2)), key1, entity1, key2, entity2);
    }

    /** {@link TableObj#insert(Object)} **/

    @Test
    default void insert_entity() {
        assumeKeys(2);
        E entity = createEntity(keys()[0]);
        assertEquals(1, table().insert(entity));

        assertTableCount(1);
        assertTableContains(keys()[0], entity);
        assertTableNotContains(keys()[1]);
        assertTableAll(entity);
    }

    @Test
    default void insert_two_entities() {
        assumeKeys(2);
        E entity1 = createEntity(keys()[0]);
        assertEquals(1, table().insert(entity1));
        E entity2 = createEntity(keys()[1]);
        assertEquals(1, table().insert(entity2));

        assertTableCount(2);
        assertTableContains(keys()[0], entity1);
        assertTableContains(keys()[1], entity2);
        assertTableAll(entity1, entity2);
    }

    @Test
    default void insert_entity_already_exists_throws() {
        assumeKeys(1);
        E entity = createEntity(keys()[0]);
        assertEquals(1, table().insert(entity));
        assertThrows(QueryException.class, () -> table().insert(entity));
    }

    /** {@link TableObj#insertIgnore(Object)} **/

    @Test
    default void insert_ignore() {
        // Ways to make it work in H2:
        // 1) URL: ";MODE=MYSQL"
        // 2) table().runner().runUpdate("SET MODE MYSQL")
        //
        // https://stackoverflow.com/questions/42364935/how-to-add-the-mode-mysql-to-embedded-h2-db-in-spring-boot-1-4-1-for-datajpates
        assumeOneOfEngines(Engine.SQLite, Engine.MySQL);

        assumeKeys(2);
        E entity = createEntity(keys()[0]);
        assertEquals(1, table().insert(entity));
        assertEquals(0, table().insertIgnore(entity));

        assertTableCount(1);
        assertTableContains(keys()[0], entity);
        assertTableNotContains(keys()[1]);
        assertTableAll(entity);
    }

    /** {@link TableObj#insertBatch(Collection)} **/

    @Test
    default void insert_batch_of_one() {
        assumeKeys(2);
        E entity = createEntity(keys()[0]);
        assertThat(table().insertBatch(List.of(entity))).asList().containsExactly(1);

        assertTableCount(1);
        assertTableContains(keys()[0], entity);
        assertTableNotContains(keys()[1]);
        assertTableAll(entity);
    }

    @Test
    default void insert_batch_of_two_different() {
        assumeKeys(2);
        E entity1 = createEntity(keys()[0]);
        E entity2 = createEntity(keys()[1]);
        assertThat(table().insertBatch(List.of(entity1, entity2))).asList().containsExactly(1, 1);

        assertTableCount(2);
        assertTableContains(keys()[0], entity1);
        assertTableContains(keys()[1], entity2);
        assertTableAll(entity1, entity2);
    }

    @Test
    default void insert_batch_of_two_duplicates() {
        assumeKeys(2);
        E entity = createEntity(keys()[0]);
        assertThrows(QueryException.class, () -> table().insertBatch(List.of(entity, entity)));

        assertTableCount(1);
        assertTableContains(keys()[0], entity);
        assertTableNotContains(keys()[1]);
        assertTableAll(entity);
    }

    /** {@link TableObj#updateByPk(Object)} **/

    @Test
    default void update_by_pk() {
        assumeKeys(2);
        table().insert(createEntity(keys()[0], 0));
        E entity = createEntity(keys()[0], 1);
        assertEquals(1, table().updateByPk(entity));

        assertTableCount(1);
        assertTableContains(keys()[0], entity);
        assertTableNotContains(keys()[1]);
        assertTableAll(entity);
    }

    @Test
    default void update_by_pk_missing_entity() {
        assumeKeys(1);
        E entity = createEntity(keys()[0]);
        assertEquals(0, table().updateByPk(entity));

        assertTableCount(0);
        assertTableNotContains(keys()[0]);
        assertThat(table().fetchAll()).isEmpty();
    }

    /** {@link TableObj#updateByPkOrInsert(Object)} **/

    @Test
    default void update_by_pk_or_insert_new_entity() {
        assumeKeys(2);
        E entity = createEntity(keys()[0]);
        assertEquals(1, table().updateByPkOrInsert(entity));

        assertTableCount(1);
        assertTableContains(keys()[0], entity);
        assertTableNotContains(keys()[1]);
        assertTableAll(entity);
    }

    @Test
    default void update_by_pk_or_insert_existing_entity() {
        assumeKeys(2);
        table().insert(createEntity(keys()[0], 0));
        E entity = createEntity(keys()[0], 1);
        assertEquals(1, table().updateByPkOrInsert(entity));

        assertTableCount(1);
        assertTableContains(keys()[0], entity);
        assertTableNotContains(keys()[1]);
        assertTableAll(entity);
    }

    /** {@link TableObj#updateWhere(Object, Where)} **/

    @Test
    default void update_where_true() {
        assumeKeys(2);
        E entity = createEntity(keys()[0], 0);
        table().insert(entity);

        E newEntity = createEntity(keys()[0], 1);
        assertEquals(1, table().updateWhere(newEntity, Where.of(Shortcuts.TRUE)));

        assertTableCount(1);
        assertTableContains(keys()[0], newEntity);
        assertTableNotContains(keys()[1]);
        assertTableAll(newEntity);
    }

    @Test
    default void update_where_false() {
        assumeKeys(2);
        E entity = createEntity(keys()[0], 0);
        table().insert(entity);

        E newEntity = createEntity(keys()[0], 1);
        assertEquals(0, table().updateWhere(newEntity, Where.of(Shortcuts.FALSE)));

        assertTableCount(1);
        assertTableContains(keys()[0], entity);
        assertTableNotContains(keys()[1]);
        assertTableAll(entity);
    }

    /** {@link TableObj#updateWhereOrInsert(Object, Where)} **/

    @Test
    default void update_where_or_insert_true() {
        assumeKeys(2);
        E entity = createEntity(keys()[0], 0);
        table().insert(entity);

        E newEntity = createEntity(keys()[0], 1);
        assertEquals(1, table().updateWhereOrInsert(newEntity, Where.of(Shortcuts.TRUE)));

        assertTableCount(1);
        assertTableContains(keys()[0], newEntity);
        assertTableNotContains(keys()[1]);
        assertTableAll(newEntity);
    }

    @Test
    default void update_where_or_insert_false() {
        assumeKeys(2);
        E entity = createEntity(keys()[0], 0);
        table().insert(entity);

        E newEntity = createEntity(keys()[1], 1);
        assertEquals(1, table().updateWhereOrInsert(newEntity, Where.of(Shortcuts.FALSE)));

        assertTableCount(2);
        assertTableContains(keys()[0], entity);
        assertTableContains(keys()[1], newEntity);
        assertTableAll(entity, newEntity);
    }


    /** {@link TableObj#deleteByPk(Object)} **/

    @Test
    default void delete_by_pk() {
        assumeKeys(1);
        table().insert(createEntity(keys()[0], 0));
        assertEquals(1, table().deleteByPk(keys()[0]));

        assertTableCount(0);
        assertTableNotContains(keys()[0]);
        assertThat(table().fetchAll()).isEmpty();
    }

    @Test
    default void delete_by_pk_missing_entity() {
        assumeKeys(1);
        assertEquals(0, table().deleteByPk(keys()[0]));

        assertTableCount(0);
        assertTableNotContains(keys()[0]);
        assertThat(table().fetchAll()).isEmpty();
    }

    /** {@link TableObj#deleteWhere(Where)} **/

    @Test
    default void delete_where_true() {
        assumeKeys(1);
        table().insert(createEntity(keys()[0], 0));
        assertEquals(1, table().deleteWhere(Where.of(Shortcuts.TRUE)));

        assertTableCount(0);
        assertTableNotContains(keys()[0]);
        assertThat(table().fetchAll()).isEmpty();
    }

    @Test
    default void delete_where_false() {
        assumeKeys(1);
        E entity = createEntity(keys()[0], 0);
        table().insert(entity);
        assertEquals(0, table().deleteWhere(Where.of(Shortcuts.FALSE)));

        assertTableCount(1);
        assertTableContains(keys()[0], entity);
        assertTableNotContains(keys()[1]);
        assertTableAll(entity);
    }

    /** {@link TableObj#fetchPage(CompositeFilter)} **/

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

        assertThat(ListBuilder.concat(page1.items(), page2.items())).containsExactly(entity1, entity2);

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

        assertEquals(count, table().count(Where.hardcoded("1 = 1", Args.of())));
        assertEquals(count > 0, table().exists(Where.hardcoded("1 = 1", Args.of())));

        assertEquals(0, table().count(Where.hardcoded("0 = 1", Args.of())));
        assertFalse(table().exists(Where.hardcoded("0 = 1", Args.of())));
    }

    default void assertTableContains(@NotNull K key, @NotNull E entity) {
        assertTrue(table().exists(key));
        assertEquals(entity, table().getByPkOrNull(key));
        assertEquals(entity, table().getByPkOrDie(key));
        assertPresent(table().getOptionalByPk(key), entity);
    }

    default void assertTableNotContains(@NotNull K key) {
        assertFalse(table().exists(key));
        assertNull(table().getByPkOrNull(key));
        assertThrows(RuntimeException.class, () -> table().getByPkOrDie(key));
        assertEmpty(table().getOptionalByPk(key));
    }

    @SafeVarargs
    private void assertTableAll(@NotNull E... entities) {
        List<E> each = new ArrayList<>();
        table().forEach(each::add);
        assertThat(each).containsExactlyElementsIn(entities);
        assertThat(table().fetchAll()).containsExactlyElementsIn(entities);
    }

    default @NotNull String findPkColumnOrDie() {
        return table().meta().sqlColumns().stream()
                .filter(TableMeta.ColumnMeta::isPrimaryKey)
                .map(TableMeta.ColumnMeta::name)
                .findFirst().orElseThrow();
    }
}
