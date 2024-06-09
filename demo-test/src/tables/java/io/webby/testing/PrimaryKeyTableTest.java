package io.webby.testing;

import com.google.common.collect.Iterators;
import com.google.common.collect.MoreCollectors;
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
import static io.webby.testing.MoreTruth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public interface PrimaryKeyTableTest<K, E, T extends TableObj<K, E>> extends BaseTableTest<E, T> {
    @NotNull K[] keys();

    @Override
    @Test
    default void empty() {
        assumeKeys(1);
        assertTableNotContains(keys()[0]);
        assertTableAll();
    }

    /** {@link TableObj#exists} **/

    @Test
    default void exists() {
        assumeKeys(1);
        assertThat(table().exists(Where.of(Shortcuts.TRUE))).isFalse();
        E entity = createEntity(keys()[0]);
        assertThat(table().insert(entity)).isEqualTo(1);
        assertThat(table().exists(Where.of(Shortcuts.TRUE))).isTrue();
    }

    /** {@link TableObj#keyOf(Object)} **/

    @Test
    default void keyOf() {
        assumeKeys(1);
        E entity = createEntity(keys()[0]);
        assertThat(table().keyOf(entity)).isEqualTo(keys()[0]);
    }

    /** {@link TableObj#fetchAllMatching(Filter)} **/

    @Test
    default void fetch_all_matching_entities() {
        assumeKeys(2);
        Where byPk = Where.of(Shortcuts.lookupBy(findPkColumnOrDie(), keyToVar(keys()[0])));
        Where all = Where.of(Shortcuts.TRUE);
        Where none = Where.of(Shortcuts.FALSE);

        assertThat(table().fetchAllMatching(byPk)).isEmpty();
        assertThat(table().fetchAllMatching(all)).isEmpty();
        assertThat(table().fetchAllMatching(none)).isEmpty();

        E entity1 = createEntity(keys()[0]);
        assertThat(table().insert(entity1)).isEqualTo(1);
        assertThat(table().fetchAllMatching(byPk)).containsExactly(entity1);
        assertThat(table().fetchAllMatching(all)).containsExactly(entity1);
        assertThat(table().fetchAllMatching(none)).isEmpty();

        E entity2 = createEntity(keys()[1]);
        assertThat(table().insert(entity2)).isEqualTo(1);
        assertThat(table().fetchAllMatching(byPk)).containsExactly(entity1);
        assertThat(table().fetchAllMatching(all)).containsExactly(entity1, entity2);
        assertThat(table().fetchAllMatching(none)).isEmpty();
    }

    /** {@link TableObj#getFirstMatchingOrNull(Filter)} **/

    @Test
    default void get_first_matching_entity() {
        assumeKeys(2);
        Where byPk = Where.of(Shortcuts.lookupBy(findPkColumnOrDie(), keyToVar(keys()[0])));
        Where all = Where.of(Shortcuts.TRUE);
        Where none = Where.of(Shortcuts.FALSE);

        assertThat(table().getFirstMatchingOrNull(byPk)).isNull();
        assertThat(table().getFirstMatchingOrNull(all)).isNull();
        assertThat(table().getFirstMatchingOrNull(none)).isNull();

        E entity1 = createEntity(keys()[0]);
        assertThat(table().insert(entity1)).isEqualTo(1);
        assertThat(table().getFirstMatchingOrNull(byPk)).isEqualTo(entity1);
        assertThat(table().getFirstMatchingOrNull(all)).isEqualTo(entity1);
        assertThat(table().getFirstMatchingOrNull(none)).isNull();

        E entity2 = createEntity(keys()[1]);
        assertThat(table().insert(entity2)).isEqualTo(1);
        assertThat(table().getFirstMatchingOrNull(byPk)).isEqualTo(entity1);
        assertThat(table().getFirstMatchingOrNull(all)).isAnyOf(entity1, entity2);
        assertThat(table().getFirstMatchingOrNull(none)).isNull();
    }

    /** {@link TableObj#getBatchByPk(Collection)} **/

    @Test
    default void getBatchByPk() {
        assumeKeys(2);
        K key1 = keys()[0];
        K key2 = keys()[1];
        assertThat(table().getBatchByPk(List.of(key1, key2))).trimmed().isEmpty();

        E entity1 = createEntity(key1);
        assertThat(table().insert(entity1)).isEqualTo(1);
        assertThat(table().getBatchByPk(List.of(key1, key2))).trimmed().containsExactly(key1, entity1);

        E entity2 = createEntity(key2);
        assertThat(table().insert(entity2)).isEqualTo(1);
        assertThat(table().getBatchByPk(List.of(key1, key2))).containsExactly(key1, entity1, key2, entity2);
    }

    /** {@link TableObj#insert(Object)} **/

    @Test
    default void insert_entity() {
        assumeKeys(2);
        E entity = createEntity(keys()[0]);
        assertThat(table().insert(entity)).isEqualTo(1);

        assertTableCount(1);
        assertTableContains(keys()[0], entity);
        assertTableNotContains(keys()[1]);
        assertTableAll(entity);
    }

    @Test
    default void insert_two_entities() {
        assumeKeys(2);
        E entity1 = createEntity(keys()[0]);
        assertThat(table().insert(entity1)).isEqualTo(1);
        E entity2 = createEntity(keys()[1]);
        assertThat(table().insert(entity2)).isEqualTo(1);

        assertTableCount(2);
        assertTableContains(keys()[0], entity1);
        assertTableContains(keys()[1], entity2);
        assertTableAll(entity1, entity2);
    }

    @Test
    default void insert_entity_already_exists_throws() {
        assumeKeys(1);
        E entity = createEntity(keys()[0]);
        assertThat(table().insert(entity)).isEqualTo(1);
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
        assumeOneOfEngines(Engine.SQLite, Engine.MySQL, Engine.MariaDB);

        assumeKeys(2);
        E entity = createEntity(keys()[0]);
        assertThat(table().insert(entity)).isEqualTo(1);
        assertThat(table().insertIgnore(entity)).isEqualTo(0);

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
        assumeNoneOfEngines(Engine.MariaDB);
        assumeKeys(2);
        E entity = createEntity(keys()[0]);
        assertThrows(QueryException.class, () -> table().insertBatch(List.of(entity, entity)));

        assertTableCount(1);
        assertTableContains(keys()[0], entity);
        assertTableNotContains(keys()[1]);
        assertTableAll(entity);
    }

    @Test
    default void insert_batch_of_two_duplicates_does_nothing() {
        assumeOneOfEngines(Engine.MariaDB);
        assumeKeys(2);
        E entity = createEntity(keys()[0]);
        assertThrows(QueryException.class, () -> table().insertBatch(List.of(entity, entity)));

        assertTableCount(0);
        assertTableNotContains(keys()[0]);
        assertThat(table().fetchAll()).isEmpty();
    }

    /** {@link TableObj#updateByPk(Object)} **/

    @Test
    default void update_by_pk() {
        assumeKeys(2);
        table().insert(createEntity(keys()[0], 0));
        E entity = createEntity(keys()[0], 1);
        assertThat(table().updateByPk(entity)).isEqualTo(1);

        assertTableCount(1);
        assertTableContains(keys()[0], entity);
        assertTableNotContains(keys()[1]);
        assertTableAll(entity);
    }

    @Test
    default void update_by_pk_missing_entity() {
        assumeKeys(1);
        E entity = createEntity(keys()[0]);
        assertThat(table().updateByPk(entity)).isEqualTo(0);

        assertTableCount(0);
        assertTableNotContains(keys()[0]);
        assertThat(table().fetchAll()).isEmpty();
    }

    /** {@link TableObj#updateByPkOrInsert(Object)} **/

    @Test
    default void update_by_pk_or_insert_new_entity() {
        assumeKeys(2);
        E entity = createEntity(keys()[0]);
        assertThat(table().updateByPkOrInsert(entity)).isEqualTo(1);

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
        assertThat(table().updateByPkOrInsert(entity)).isEqualTo(1);

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
        assertThat(table().updateWhere(newEntity, Where.of(Shortcuts.TRUE))).isEqualTo(1);

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
        assertThat(table().updateWhere(newEntity, Where.of(Shortcuts.FALSE))).isEqualTo(0);

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
        assertThat(table().updateWhereOrInsert(newEntity, Where.of(Shortcuts.TRUE))).isEqualTo(1);

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
        assertThat(table().updateWhereOrInsert(newEntity, Where.of(Shortcuts.FALSE))).isEqualTo(1);

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
        assertThat(table().deleteByPk(keys()[0])).isEqualTo(1);

        assertTableCount(0);
        assertTableNotContains(keys()[0]);
        assertThat(table().fetchAll()).isEmpty();
    }

    @Test
    default void delete_by_pk_missing_entity() {
        assumeKeys(1);
        assertThat(table().deleteByPk(keys()[0])).isEqualTo(0);

        assertTableCount(0);
        assertTableNotContains(keys()[0]);
        assertThat(table().fetchAll()).isEmpty();
    }

    /** {@link TableObj#deleteWhere(Where)} **/

    @Test
    default void delete_where_true() {
        assumeKeys(1);
        table().insert(createEntity(keys()[0], 0));
        assertThat(table().deleteWhere(Where.of(Shortcuts.TRUE))).isEqualTo(1);

        assertTableCount(0);
        assertTableNotContains(keys()[0]);
        assertThat(table().fetchAll()).isEmpty();
    }

    @Test
    default void delete_where_false() {
        assumeKeys(1);
        E entity = createEntity(keys()[0], 0);
        table().insert(entity);
        assertThat(table().deleteWhere(Where.of(Shortcuts.FALSE))).isEqualTo(0);

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
        assertThat(table().insert(entity1)).isEqualTo(1);
        E entity2 = createEntity(keys()[1]);
        assertThat(table().insert(entity2)).isEqualTo(1);

        CompositeFilter firstPageClause = CompositeFilter.builder().with(Pagination.firstPage(1), table().engine()).build();
        Page<E> page1 = table().fetchPage(firstPageClause);
        assertThat(page1.hasNextPage()).isTrue();
        assertThat(page1.nextToken()).isNotNull();
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
        assertThat(table().count()).isEqualTo(count);
        assertThat(table().isEmpty()).isEqualTo(count == 0);
        assertThat(table().isNotEmpty()).isEqualTo(count > 0);

        assertThat(table().count(Where.hardcoded("1 = 1", Args.of()))).isEqualTo(count);
        assertThat(table().exists(Where.hardcoded("1 = 1", Args.of()))).isEqualTo(count > 0);

        assertThat(table().count(Where.hardcoded("0 = 1", Args.of()))).isEqualTo(0);
        assertThat(table().exists(Where.hardcoded("0 = 1", Args.of()))).isFalse();
    }

    default void assertTableContains(@NotNull K key, @NotNull E entity) {
        assertThat(table().exists(key)).isTrue();
        assertThat(table().getByPkOrNull(key)).isEqualTo(entity);
        assertThat(table().getByPkOrDie(key)).isEqualTo(entity);
        assertThat(table().getOptionalByPk(key)).hasValue(entity);
    }

    default void assertTableNotContains(@NotNull K key) {
        assertThat(table().exists(key)).isFalse();
        assertThat(table().getByPkOrNull(key)).isNull();
        assertThat(table().getOptionalByPk(key)).isEmpty();
        assertThrows(RuntimeException.class, () -> table().getByPkOrDie(key));
    }

    @SafeVarargs
    private void assertTableAll(@NotNull E... entities) {
        assertTableCount(entities.length);
        assertThat(forEachToJavaList()).containsExactlyElementsIn(entities);
        assertThat(forEachToJavaList(Where.of(Shortcuts.TRUE))).containsExactlyElementsIn(entities);
        assertThat(iteratorToJavaList()).containsExactlyElementsIn(entities);
        assertThat(iteratorToJavaList(Where.of(Shortcuts.TRUE))).containsExactlyElementsIn(entities);
        assertThat(table().fetchAll()).containsExactlyElementsIn(entities);
        assertThat(table().fetchAllMatching(Where.of(Shortcuts.TRUE))).containsExactlyElementsIn(entities);
    }

    default @NotNull Column findPkColumnOrDie() {
        return table().meta().sqlColumns().stream()
            .filter(TableMeta.ColumnMeta::isPrimaryKey)
            .map(TableMeta.ColumnMeta::column)
            .collect(MoreCollectors.onlyElement());
    }

    default @NotNull Variable keyToVar(@NotNull K key) {
        return new Variable(key, TermType.WILDCARD);
    }

    private @NotNull List<E> forEachToJavaList() {
        List<E> result = new ArrayList<>();
        table().forEach(result::add);
        return result;
    }

    private @NotNull List<E> forEachToJavaList(@NotNull Filter filter) {
        List<E> result = new ArrayList<>();
        table().forEach(filter, result::add);
        return result;
    }

    private @NotNull List<E> iteratorToJavaList() {
        List<E> result = new ArrayList<>();
        try (ResultSetIterator<E> iterator = table().iterator()) {
            Iterators.addAll(result, iterator);
        }
        return result;
    }

    private @NotNull List<E> iteratorToJavaList(@NotNull Filter filter) {
        List<E> result = new ArrayList<>();
        try (ResultSetIterator<E> iterator = table().iterator(filter)) {
            Iterators.addAll(result, iterator);
        }
        return result;
    }
}
