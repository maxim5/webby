package io.webby.auth.user;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class UserTableTest {
    private static final String URL = "jdbc:sqlite:%s".formatted(":memory:");

    private Connection connection;
    private UserTable userTable;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection(URL);
        connection.createStatement().executeUpdate("CREATE TABLE user (userId INTEGER PRIMARY KEY, access INTEGER)");
        userTable = new UserTable(connection);
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void empty() {
        assertEquals(0, userTable.count());
        assertTrue(userTable.isEmpty());
        assertNull(userTable.getByPkOrNull(1));
        assertThat(userTable.fetchAll()).isEmpty();
    }

    @Test
    public void insert_user() {
        DefaultUser user = new DefaultUser(1, UserAccess.Simple);
        assertEquals(1, userTable.insert(user));

        assertEquals(1, userTable.count());
        assertFalse(userTable.isEmpty());
        assertEquals(user, userTable.getByPkOrNull(1));
        assertNull(userTable.getByPkOrNull(2));
        assertThat(userTable.fetchAll()).containsExactly(user);
    }

    @Test
    public void update_user() {
        userTable.insert(new DefaultUser(1, UserAccess.Simple));
        DefaultUser user = new DefaultUser(1, UserAccess.Admin);
        assertEquals(1, userTable.updateByPk(user));

        assertEquals(1, userTable.count());
        assertFalse(userTable.isEmpty());
        assertEquals(user, userTable.getByPkOrNull(1));
        assertNull(userTable.getByPkOrNull(2));
        assertThat(userTable.fetchAll()).containsExactly(user);
    }
}
