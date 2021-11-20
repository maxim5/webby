package io.webby.testing;

import io.webby.db.sql.SqlSettings;
import io.webby.util.sql.api.QueryRunner;
import io.webby.util.sql.api.DebugSql;
import io.webby.util.sql.api.TableObj;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public abstract class BaseModelKeyTableTest<K, E, T extends TableObj<K, E>> {
    private static final String URL = SqlSettings.SQLITE_IN_MEMORY;

    protected Connection connection;
    protected T table;
    protected K[] keys;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection(URL);
        setUp(connection);
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
    }

    protected abstract void setUp(@NotNull Connection connection) throws Exception;

    @Test
    public void column_names() throws Exception {
        String tableName = table.meta().sqlTableName();
        List<String> expectedColumns = parseColumnNamesFromDb(tableName);
        assertThat(table.meta().sqlColumns()).containsExactlyElementsIn(expectedColumns);
    }

    @Test
    public void empty() {
        assumeKeys(1);
        assertEquals(0, table.count());
        assertTrue(table.isEmpty());
        assertNull(table.getByPkOrNull(keys[0]));
        assertThat(table.fetchAll()).isEmpty();
    }

    @Test
    public void key_of_entity() {
        assumeKeys(1);
        E entity = createEntity(keys[0]);
        assertEquals(keys[0], table.keyOf(entity));
    }

    @Test
    public void insert_entity() {
        assumeKeys(2);
        E entity = createEntity(keys[0]);
        assertEquals(1, table.insert(entity));

        assertEquals(1, table.count());
        assertFalse(table.isEmpty());
        assertEquals(entity, table.getByPkOrNull(keys[0]));
        assertNull(table.getByPkOrNull(keys[1]));
        assertThat(table.fetchAll()).containsExactly(entity);
    }

    @Test
    public void insert_two_entities() {
        assumeKeys(2);
        E entity1 = createEntity(keys[0]);
        assertEquals(1, table.insert(entity1));
        E entity2 = createEntity(keys[1]);
        assertEquals(1, table.insert(entity2));

        assertEquals(2, table.count());
        assertFalse(table.isEmpty());
        assertEquals(entity1, table.getByPkOrNull(keys[0]));
        assertEquals(entity2, table.getByPkOrNull(keys[1]));
        assertThat(table.fetchAll()).containsExactly(entity1, entity2);
    }

    @Test
    public void update_entity() {
        assumeKeys(2);
        table.insert(createEntity(keys[0], 0));
        E entity = createEntity(keys[0], 1);
        assertEquals(1, table.updateByPk(entity));

        assertEquals(1, table.count());
        assertFalse(table.isEmpty());
        assertEquals(entity, table.getByPkOrNull(keys[0]));
        assertNull(table.getByPkOrNull(keys[1]));
        assertThat(table.fetchAll()).containsExactly(entity);
    }

    @Test
    public void update_missing_entity() {
        assumeKeys(1);
        E entity = createEntity(keys[0]);
        assertEquals(0, table.updateByPk(entity));

        assertEquals(0, table.count());
        assertTrue(table.isEmpty());
        assertNull(table.getByPkOrNull(keys[0]));
        assertThat(table.fetchAll()).isEmpty();
    }

    @Test
    public void update_or_insert_new_entity() {
        assumeKeys(2);
        E entity = createEntity(keys[0]);
        assertEquals(1, table.updateByPkOrInsert(entity));

        assertEquals(1, table.count());
        assertFalse(table.isEmpty());
        assertEquals(entity, table.getByPkOrNull(keys[0]));
        assertNull(table.getByPkOrNull(keys[1]));
        assertThat(table.fetchAll()).containsExactly(entity);
    }

    @Test
    public void update_or_insert_existing_entity() {
        assumeKeys(2);
        table.insert(createEntity(keys[0], 0));
        E entity = createEntity(keys[0], 1);
        assertEquals(1, table.updateByPkOrInsert(entity));

        assertEquals(1, table.count());
        assertFalse(table.isEmpty());
        assertEquals(entity, table.getByPkOrNull(keys[0]));
        assertNull(table.getByPkOrNull(keys[1]));
        assertThat(table.fetchAll()).containsExactly(entity);
    }

    @Test
    public void delete_entity() {
        assumeKeys(1);
        table.insert(createEntity(keys[0], 0));
        assertEquals(1, table.deleteByPk(keys[0]));

        assertEquals(0, table.count());
        assertTrue(table.isEmpty());
        assertNull(table.getByPkOrNull(keys[0]));
        assertThat(table.fetchAll()).isEmpty();
    }

    @Test
    public void delete_missing_entity() {
        assumeKeys(1);
        assertEquals(0, table.deleteByPk(keys[0]));

        assertEquals(0, table.count());
        assertTrue(table.isEmpty());
        assertNull(table.getByPkOrNull(keys[0]));
        assertThat(table.fetchAll()).isEmpty();
    }

    protected @NotNull E createEntity(@NotNull K key) {
        return createEntity(key, 0);
    }

    protected abstract @NotNull E createEntity(@NotNull K key, int version);

    protected void assumeKeys(int minimumNum) {
        assertNotNull(keys);
        assumeTrue(keys.length >= minimumNum,
                   "Can't run the test because not enough keys available: %s".formatted(Arrays.toString(keys)));
    }

    protected List<String> parseColumnNamesFromDb(@NotNull String name) throws SQLException {
        ResultSet resultSet = new QueryRunner(connection).runQuery("SELECT sql FROM sqlite_master WHERE name=?", name);
        List<DebugSql.Row> rows = DebugSql.toDebugRows(resultSet);
        assertFalse(rows.isEmpty(), "SQL table not found in DB: %s".formatted(name));
        assertThat(rows).hasSize(1);

        String sql = rows.get(0).findValue("sql")
                .map(DebugSql.RowValue::value)
                .orElseThrow()
                .replaceAll("\\s+", " ")
                .replaceFirst(".*\\((.*?)\\)", "$1");

        return Arrays.stream(sql.split(","))
                .map(String::trim)
                .map(line -> line.replaceFirst("(\\w+) .*", "$1"))
                .toList();
    }
}
