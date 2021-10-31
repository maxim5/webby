package io.webby.testing;

import io.webby.util.sql.api.TableObj;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public abstract class BaseModelKeyTableTest<K, E, T extends TableObj<K, E>> {
    private static final String URL = "jdbc:sqlite:%s".formatted(":memory:");

    protected Connection connection;
    protected T table;
    protected K key1;
    protected K key2;

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
    public void empty() {
        assertEquals(0, table.count());
        assertTrue(table.isEmpty());
        assertNull(table.getByPkOrNull(key1));
        assertThat(table.fetchAll()).isEmpty();
    }

    @Test
    public void insert_entity() {
        E entity = createEntity(key1);
        assertEquals(1, table.insert(entity));

        assertEquals(1, table.count());
        assertFalse(table.isEmpty());
        assertEquals(entity, table.getByPkOrNull(key1));
        assertNull(table.getByPkOrNull(key2));
        assertThat(table.fetchAll()).containsExactly(entity);
    }

    @Test
    public void update_entity() {
        table.insert(createEntity(key1, 0));
        E entity = createEntity(key1, 1);
        assertEquals(1, table.updateByPk(entity));

        assertEquals(1, table.count());
        assertFalse(table.isEmpty());
        assertEquals(entity, table.getByPkOrNull(key1));
        assertNull(table.getByPkOrNull(key2));
        assertThat(table.fetchAll()).containsExactly(entity);
    }

    protected @NotNull E createEntity(@NotNull K key) {
        return createEntity(key, 0);
    }

    protected abstract @NotNull E createEntity(@NotNull K key, int version);
}
